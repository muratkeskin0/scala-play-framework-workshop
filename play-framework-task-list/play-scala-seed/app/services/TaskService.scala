package services

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import repositories._
import models._

sealed trait TaskError { def message: String }
object TaskError {
  final case class InvalidInput(message: String) extends TaskError
  case object TaskNotFound extends TaskError {
    val message = "Task not found."
  }
  case object InvalidTaskId extends TaskError {
    val message = "Invalid task ID."
  }
  case object TaskCreationFailed extends TaskError {
    val message = "Could not create task."
  }
  case object TaskDeletionFailed extends TaskError {
    val message = "Could not delete task."
  }
  case object UserNotFound extends TaskError {
    val message = "User not found."
  }
}

trait ITaskService {
  def list(username: String): Future[Seq[Task]]
  def add(username: String, taskDescription: String): Future[Either[TaskError, Task]]
  def delete(username: String, taskId: Long): Future[Either[TaskError, Unit]]
  def get(username: String, taskId: Long): Future[Either[TaskError, Task]]
  def update(username: String, taskId: Long, taskDescription: String): Future[Either[TaskError, Task]]
  def countByEmails(emails: Seq[String]): Future[Map[String, Int]]
}

@Singleton
class TaskService @Inject()(taskRepo: ITaskRepository, userRepo: IUserRepository)(implicit ec: ExecutionContext) extends ITaskService {
  import TaskError._

  private def validTaskDescription(description: String): Boolean =
    description.trim.nonEmpty

  /** Helper to resolve username → userId */
  private def withUserId[A](username: String)(f: Long => Future[Either[TaskError, A]]): Future[Either[TaskError, A]] =
    userRepo.get(username).flatMap {
      case Some(user) => f(user.id)                // User(id, username, password)
      case None       => Future.successful(Left(UserNotFound))
    }

  /** Listing now comes from DB with Future */
  override def list(username: String): Future[Seq[Task]] =
    withUserId[Seq[Task]](username) { uid =>
      taskRepo.getAll(uid).map(tasks => Right(tasks))
    }.map {
      case Right(tasks) => tasks
      case Left(_)      => Seq.empty
    }

  /** Adding: username → userId, then INSERT returning id */
  override def add(username: String, taskDescription: String): Future[Either[TaskError, Task]] = {
    val trimmed = taskDescription.trim
    if (!validTaskDescription(trimmed))
      Future.successful(Left(InvalidInput("Please enter a valid task description.")))
    else
      withUserId[Task](username) { uid =>
        val draft = Task(id = 0L, userId = uid, description = trimmed)
        taskRepo.add(draft).map { newId =>
          if (newId > 0) Right(draft.copy(id = newId))
          else Left(TaskCreationFailed)
        }
      }
  }

  /**
   * ID-based deletion:
   *  - Verify task belongs to user
   *  - Delete by taskId directly
   */
  override def delete(username: String, taskId: Long): Future[Either[TaskError, Unit]] = {
    if (taskId <= 0) return Future.successful(Left(InvalidTaskId))
    withUserId[Unit](username) { uid =>
      // First verify the task belongs to this user
      taskRepo.get(taskId).flatMap {
        case Some(task) if task.userId == uid =>
          taskRepo.delete(taskId).map { affected =>
            if (affected > 0) Right(()) else Left(TaskDeletionFailed)
          }
        case Some(_) =>
          Future.successful(Left(TaskNotFound)) // Task exists but doesn't belong to user
        case None =>
          Future.successful(Left(TaskNotFound)) // Task doesn't exist
      }
    }
  }

  /** ID-based get: select task by ID and verify ownership */
  override def get(username: String, taskId: Long): Future[Either[TaskError, Task]] = {
    if (taskId <= 0) return Future.successful(Left(InvalidTaskId))
    withUserId[Task](username) { uid =>
      taskRepo.get(taskId).map {
        case Some(task) if task.userId == uid => Right(task)
        case Some(_) => Left(TaskNotFound) // Task exists but doesn't belong to user
        case None => Left(TaskNotFound) // Task doesn't exist
      }
    }
  }

  /** ID-based update: find task by ID, verify ownership, then UPDATE */
  override def update(username: String, taskId: Long, taskDescription: String): Future[Either[TaskError, Task]] = {
    val trimmed = taskDescription.trim
    if (!validTaskDescription(trimmed))
      return Future.successful(Left(InvalidInput("Please enter a valid task description.")))
    if (taskId <= 0)
      return Future.successful(Left(InvalidTaskId))

    withUserId[Task](username) { uid =>
      taskRepo.get(taskId).flatMap {
        case Some(task) if task.userId == uid =>
          val updated = task.copy(description = trimmed) // userId/id stays the same
          taskRepo.update(updated).map { affected =>
            if (affected > 0) Right(updated) else Left(TaskNotFound)
          }
        case Some(_) =>
          Future.successful(Left(TaskNotFound)) // Task exists but doesn't belong to user
        case None =>
          Future.successful(Left(TaskNotFound)) // Task doesn't exist
      }
    }
  }
  /** Get bulk task count for multiple users */
  override def countByEmails(emails: Seq[String]): Future[Map[String, Int]] = {
    if (emails.isEmpty) Future.successful(Map.empty)
    else {
      // Get users at once and create email->id mapping
      userRepo.list().flatMap { users =>
        val selected = users.filter(u => emails.contains(u.email))
        val emailToId = selected.map(u => u.email -> u.id).toMap
        taskRepo.countByUserIds(emailToId.values.toSeq).map { uidToCount =>
          emailToId.map { case (email, uid) =>
            email -> uidToCount.getOrElse(uid, 0)
          }
        }
      }
    }
  }
}
