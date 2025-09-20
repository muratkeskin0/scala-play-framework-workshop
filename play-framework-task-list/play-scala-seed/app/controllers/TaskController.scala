package controllers

import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.i18n.I18nSupport
import services.{ITaskService, IUserService}
import forms.TaskForms._
import models.{User, Role}
import org.pac4j.core.profile.CommonProfile
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaskController @Inject()(val controllerComponents: ControllerComponents, taskService: ITaskService, userService: IUserService)(implicit ec: ExecutionContext) extends BaseController with I18nSupport {

  // Helper method to get current user (filter ensures authentication)
  private def getCurrentUser(request: Request[_]): Future[Option[User]] = {
    request.session.get("email") match {
      case Some(email) => userService.get(email)
      case None => Future.successful(None)
    }
  }

  def taskList() = Action.async { implicit request =>
    getCurrentUser(request).flatMap {
      case Some(user) =>
        taskService.list(user.email).map { tasks =>
          Ok(views.html.taskList(tasks, Some(user)))
        }
      case None =>
        Future.successful(Redirect(routes.AuthController.login()).flashing("error" -> "User not found"))
    }
  }

  def addTask() = Action.async { implicit request =>
    getCurrentUser(request).flatMap {
      case Some(user) =>
        addTaskForm.bindFromRequest().fold(
          formWithErrors => {
            Future.successful(
              Redirect(routes.TaskController.taskList())
                .flashing("error" -> "Please enter a valid task description.")
            )
          },
          taskData => {
            taskService.add(user.email, taskData.description).map {
              case Right(_)  => Redirect(routes.TaskController.taskList()).flashing("success" -> "Task added!")
              case Left(err) => Redirect(routes.TaskController.taskList()).flashing("error"   -> err.message)
            }.recover { case _ =>
              Redirect(routes.TaskController.taskList()).flashing("error" -> "Unexpected error while adding task")
            }
          }
        )
      case None =>
        Future.successful(Redirect(routes.AuthController.login()).flashing("error" -> "User not found"))
    }
  }

  def updateTask() = Action.async { implicit request =>
    getCurrentUser(request).flatMap {
      case Some(user) =>
        updateTaskForm.bindFromRequest().fold(
          formWithErrors => {
            Future.successful(
              Redirect(routes.TaskController.taskList())
                .flashing("error" -> "Please enter a valid task description.")
            )
          },
          updateData => {
            taskService.update(user.email, updateData.id, updateData.description).map {
              case Right(_)  => Redirect(routes.TaskController.taskList()).flashing("success" -> "Task updated!")
              case Left(err) => Redirect(routes.TaskController.taskList()).flashing("error"   -> err.message)
            }.recover { case _ =>
              Redirect(routes.TaskController.taskList()).flashing("error" -> "Unexpected error while updating task")
            }
          }
        )
      case None =>
        Future.successful(Redirect(routes.AuthController.login()).flashing("error" -> "User not found"))
    }
  }

  def deleteTask() = Action.async { implicit request =>
    getCurrentUser(request).flatMap {
      case Some(user) =>
        deleteTaskForm.bindFromRequest().fold(
          formWithErrors => {
            Future.successful(
              Redirect(routes.TaskController.taskList())
                .flashing("error" -> "Invalid task ID.")
            )
          },
          deleteData => {
            taskService.delete(user.email, deleteData.id).map {
              case Right(_)  => Redirect(routes.TaskController.taskList()).flashing("success" -> "Task deleted!")
              case Left(err) => Redirect(routes.TaskController.taskList()).flashing("error"   -> err.message)
            }.recover { case _ =>
              Redirect(routes.TaskController.taskList()).flashing("error" -> "Unexpected error while deleting task")
            }
          }
        )
      case None =>
        Future.successful(Redirect(routes.AuthController.login()).flashing("error" -> "User not found"))
    }
  }
}
