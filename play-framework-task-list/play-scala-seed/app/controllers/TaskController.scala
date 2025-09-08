package controllers

import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.i18n.I18nSupport
import services.ITaskService
import forms.TaskForms._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaskController @Inject()(val controllerComponents: ControllerComponents, taskService: ITaskService)(implicit ec: ExecutionContext) extends BaseController with I18nSupport {

  def taskList() = Action.async { implicit request =>
    request.session.get("email") match {
      case None =>
        Future.successful(Unauthorized("Unauthorized"))
      case Some(email) =>
        taskService.list(email).map { tasks =>
          Ok(views.html.taskList(tasks))
        }.recover { case _ =>
          InternalServerError("Failed to load tasks")
        }
    }
  }

  def addTask() = Action.async { implicit request =>
    request.session.get("email") match {
      case None =>
        Future.successful(Unauthorized("Unauthorized"))
      case Some(email) =>
        addTaskForm.bindFromRequest().fold(
          formWithErrors => {
            Future.successful(
              Redirect(routes.TaskController.taskList())
                .flashing("error" -> "Please enter a valid task description.")
            )
          },
          taskData => {
            taskService.add(email, taskData.description).map {
              case Right(_)  => Redirect(routes.TaskController.taskList()).flashing("success" -> "Task added!")
              case Left(err) => Redirect(routes.TaskController.taskList()).flashing("error"   -> err.message)
            }.recover { case _ =>
              Redirect(routes.TaskController.taskList()).flashing("error" -> "Unexpected error while adding task")
            }
          }
        )
    }
  }

  def updateTask() = Action.async { implicit request =>
    request.session.get("email") match {
      case None =>
        Future.successful(Unauthorized("Unauthorized"))
      case Some(email) =>
        updateTaskForm.bindFromRequest().fold(
          formWithErrors => {
            Future.successful(
              Redirect(routes.TaskController.taskList())
                .flashing("error" -> "Please enter a valid task description.")
            )
          },
          updateData => {
            taskService.update(email, updateData.id, updateData.description).map {
              case Right(_)  => Redirect(routes.TaskController.taskList()).flashing("success" -> "Task updated!")
              case Left(err) => Redirect(routes.TaskController.taskList()).flashing("error"   -> err.message)
            }.recover { case _ =>
              Redirect(routes.TaskController.taskList()).flashing("error" -> "Unexpected error while updating task")
            }
          }
        )
    }
  }

  def deleteTask() = Action.async { implicit request =>
    request.session.get("email") match {
      case None =>
        Future.successful(Unauthorized("Unauthorized"))
      case Some(email) =>
        deleteTaskForm.bindFromRequest().fold(
          formWithErrors => {
            Future.successful(
              Redirect(routes.TaskController.taskList())
                .flashing("error" -> "Invalid task ID.")
            )
          },
          deleteData => {
            taskService.delete(email, deleteData.id).map {
              case Right(_)  => Redirect(routes.TaskController.taskList()).flashing("success" -> "Task deleted!")
              case Left(err) => Redirect(routes.TaskController.taskList()).flashing("error"   -> err.message)
            }.recover { case _ =>
              Redirect(routes.TaskController.taskList()).flashing("error" -> "Unexpected error while deleting task")
            }
          }
        )
    }
  }
}
