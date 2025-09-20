package controllers

import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.i18n.I18nSupport
import services.ITaskService
import forms.TaskForms._
import security.SecurityModule
import org.pac4j.core.profile.CommonProfile
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaskController @Inject()(val controllerComponents: ControllerComponents, taskService: ITaskService, securityModule: SecurityModule)(implicit ec: ExecutionContext) extends BaseController with I18nSupport {

  // Helper method to get user email from session
  private def getCurrentUserEmail(request: Request[_]): Option[String] = {
    println(s"Getting user email from request...")
    
    request.session.get("email") match {
      case Some(email) =>
        println(s"Found email in session: $email")
        Some(email)
      case None =>
        println(s"No email found in session")
        None
    }
  }

  def taskList() = Action.async { implicit request =>
    if (securityModule.isAuthenticated(request)) {
      getCurrentUserEmail(request) match {
        case Some(email) =>
          taskService.list(email).map { tasks =>
            Ok(views.html.taskList(tasks))
          }.recover { case _ =>
            InternalServerError("Failed to load tasks")
          }
        case None =>
          Future.successful(Unauthorized("Unauthorized"))
      }
    } else {
      Future.successful(Results.Redirect(controllers.routes.AuthController.login())
        .flashing("error" -> "Please log in to access this page."))
    }
  }

  def addTask() = Action.async { implicit request =>
    if (securityModule.isAuthenticated(request)) {
      getCurrentUserEmail(request) match {
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
        case None =>
          Future.successful(Unauthorized("Unauthorized"))
      }
    } else {
      Future.successful(Results.Redirect(controllers.routes.AuthController.login())
        .flashing("error" -> "Please log in to access this page."))
    }
  }

  def updateTask() = Action.async { implicit request =>
    if (securityModule.isAuthenticated(request)) {
      getCurrentUserEmail(request) match {
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
        case None =>
          Future.successful(Unauthorized("Unauthorized"))
      }
    } else {
      Future.successful(Results.Redirect(controllers.routes.AuthController.login())
        .flashing("error" -> "Please log in to access this page."))
    }
  }

  def deleteTask() = Action.async { implicit request =>
    if (securityModule.isAuthenticated(request)) {
      getCurrentUserEmail(request) match {
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
        case None =>
          Future.successful(Unauthorized("Unauthorized"))
      }
    } else {
      Future.successful(Results.Redirect(controllers.routes.AuthController.login())
        .flashing("error" -> "Please log in to access this page."))
    }
  }
}
