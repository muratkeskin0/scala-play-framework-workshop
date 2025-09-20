package controllers

import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.i18n.I18nSupport
import services.{ITaskService, IUserService}
import forms.TaskForms._
import security.SecurityModule
import models.{User, Role}
import org.pac4j.core.profile.CommonProfile
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaskController @Inject()(val controllerComponents: ControllerComponents, taskService: ITaskService, userService: IUserService, securityModule: SecurityModule)(implicit ec: ExecutionContext) extends BaseController with I18nSupport {

  // Helper method to get user email from session
  private def getCurrentUserEmail(request: Request[_]): Option[String] = {
    request.session.get("email")
  }
  
  // Helper method to check if user is authenticated
  private def requireAuth(request: Request[_]): Future[Either[Result, User]] = {
    getCurrentUserEmail(request) match {
      case Some(email) =>
        userService.get(email).map {
          case Some(user) => Right(user)
          case None => Left(Redirect(routes.AuthController.login()).flashing("error" -> "User not found"))
        }
      case None =>
        Future.successful(Left(Redirect(routes.AuthController.login()).flashing("error" -> "Please login to access this page")))
    }
  }

  def taskList() = Action.async { implicit request =>
    requireAuth(request).flatMap {
      case Right(user) =>
        taskService.list(user.email).map { tasks =>
          Ok(views.html.taskList(tasks, Some(user)))
        }
      case Left(result) => Future.successful(result)
    }
  }

  def addTask() = Action.async { implicit request =>
    requireAuth(request).flatMap {
      case Right(user) =>
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
      case Left(result) => Future.successful(result)
    }
  }

  def updateTask() = Action.async { implicit request =>
    requireAuth(request).flatMap {
      case Right(user) =>
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
      case Left(result) => Future.successful(result)
    }
  }

  def deleteTask() = Action.async { implicit request =>
    requireAuth(request).flatMap {
      case Right(user) =>
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
      case Left(result) => Future.successful(result)
    }
  }
}
