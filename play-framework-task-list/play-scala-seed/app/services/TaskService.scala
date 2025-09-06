package services

import javax.inject._
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
}

trait ITaskService {
  def list(username: String): Seq[Task]
  def add(username: String, taskDescription: String): Either[TaskError, Task]
  def delete(username: String, index: Int): Either[TaskError, Unit]
  def get(username: String, index: Int): Either[TaskError, Task]
  def update(username: String, index: Int, taskDescription: String): Either[TaskError, Task]
}

@Singleton
class TaskService @Inject()(repo: ITaskRepository) extends ITaskService {
  import TaskError._

  private def validTaskDescription(description: String): Boolean =
    description.trim.nonEmpty

  override def list(username: String): Seq[Task] =
    repo.getAll(username)

  override def add(username: String, taskDescription: String): Either[TaskError, Task] = {
    val trimmedDescription = taskDescription.trim
    if (!validTaskDescription(trimmedDescription)) {
      Left(InvalidInput("Please enter a valid task description."))
    } else {
      val task = Task(username, trimmedDescription)
      if (repo.add(task)) Right(task) else Left(TaskCreationFailed)
    }
  }

  override def delete(username: String, index: Int): Either[TaskError, Unit] = {
    if (index < 0) {
      Left(InvalidIndex)
    } else {
      val tasks = repo.getAll(username)
      if (index >= tasks.length) {
        Left(TaskNotFound)
      } else {
        if (repo.delete(username, index)) Right(()) else Left(TaskDeletionFailed)
      }
    }
  }

  override def get(username: String, index: Int): Either[TaskError, Task] = {
    if (index < 0) {
      Left(InvalidIndex)
    } else {
      repo.get(username, index) match {
        case Some(task) => Right(task)
        case None => Left(TaskNotFound)
      }
    }
  }

  override def update(username: String, index: Int, taskDescription: String): Either[TaskError, Task] = {
    val trimmedDescription = taskDescription.trim
    if (!validTaskDescription(trimmedDescription)) {
      Left(InvalidInput("Please enter a valid task description."))
    } else if (index < 0) {
      Left(InvalidIndex)
    } else {
      val updatedTask = Task(username, trimmedDescription)
      if (repo.update(username, index, updatedTask)) {
        Right(updatedTask)
      } else {
        Left(TaskNotFound)
      }
    }
  }
}
