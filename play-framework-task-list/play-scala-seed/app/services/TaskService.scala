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
  case object InvalidIndex extends TaskError {
    val message = "Invalid task index."
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
  def delete(username: String, index: Int): Future[Either[TaskError, Unit]]
  def get(username: String, index: Int): Future[Either[TaskError, Task]]
  def update(username: String, index: Int, taskDescription: String): Future[Either[TaskError, Task]]
}

@Singleton
class TaskService @Inject()(taskRepo: ITaskRepository, userRepo: IUserRepository)(implicit ec: ExecutionContext) extends ITaskService {
  import TaskError._

  private def validTaskDescription(description: String): Boolean =
    description.trim.nonEmpty

  /** username → userId çözümleme helper’ı */
  private def withUserId[A](username: String)(f: Long => Future[Either[TaskError, A]]): Future[Either[TaskError, A]] =
    userRepo.get(username).flatMap {
      case Some(user) => f(user.id)                // User(id, username, password)
      case None       => Future.successful(Left(UserNotFound))
    }

  /** Listeleme artık DB’den Future ile gelir */
  override def list(username: String): Future[Seq[Task]] =
    withUserId[Seq[Task]](username) { uid =>
      taskRepo.getAll(uid).map(tasks => Right(tasks))
    }.map {
      case Right(tasks) => tasks
      case Left(_)      => Seq.empty
    }

  /** Ekleme: username → userId, sonra INSERT returning id */
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
   * Index bazlı silme:
   *  - Kullanıcının task’larını belirli bir sıraya göre (id ASC) çek
   *  - index’ten taskId’yi bul
   *  - taskId ile sil
   */
  override def delete(username: String, index: Int): Future[Either[TaskError, Unit]] = {
    if (index < 0) return Future.successful(Left(InvalidIndex))
    withUserId[Unit](username) { uid =>
      taskRepo.getAll(uid).flatMap { tasks =>
        val ordered = tasks.sortBy(_.id) // basit sıralama; istersen createdAt ekleyebilirsin
        if (index >= ordered.length) Future.successful(Left(TaskNotFound))
        else {
          val taskId = ordered(index).id
          taskRepo.delete(taskId).map { affected =>
            if (affected > 0) Right(()) else Left(TaskDeletionFailed)
          }
        }
      }
    }
  }

  /** Index bazlı getir: listeden index’e göre seç */
  override def get(username: String, index: Int): Future[Either[TaskError, Task]] = {
    if (index < 0) return Future.successful(Left(InvalidIndex))
    withUserId[Task](username) { uid =>
      taskRepo.getAll(uid).map { tasks =>
        val ordered = tasks.sortBy(_.id)
        if (index >= ordered.length) Left(TaskNotFound)
        else Right(ordered(index))
      }
    }
  }

  /** Index bazlı güncelle: listeden taskId’yi bul, sonra UPDATE */
  override def update(username: String, index: Int, taskDescription: String): Future[Either[TaskError, Task]] = {
    val trimmed = taskDescription.trim
    if (!validTaskDescription(trimmed))
      return Future.successful(Left(InvalidInput("Please enter a valid task description.")))
    if (index < 0)
      return Future.successful(Left(InvalidIndex))

    withUserId[Task](username) { uid =>
      taskRepo.getAll(uid).flatMap { tasks =>
        val ordered = tasks.sortBy(_.id)
        if (index >= ordered.length) Future.successful(Left(TaskNotFound))
        else {
          val current = ordered(index)
          val updated = current.copy(description = trimmed) // userId/id aynı kalır
          taskRepo.update(updated).map { affected =>
            if (affected > 0) Right(updated) else Left(TaskNotFound)
          }
        }
      }
    }
  }
}
