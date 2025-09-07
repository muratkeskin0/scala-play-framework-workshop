package controllers

import javax.inject._
import play.api.mvc._
import services.ITaskService
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaskController @Inject()(val controllerComponents: ControllerComponents, taskService: ITaskService)(implicit ec: ExecutionContext) extends BaseController {

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
    val form    = request.body.asFormUrlEncoded.getOrElse(Map.empty)
    val rawTask = form.get("task").flatMap(_.headOption).getOrElse("").trim

    request.session.get("email") match {
      case None =>
        Future.successful(Unauthorized("Unauthorized"))
      case Some(email) =>
        taskService.add(email, rawTask).map {
          case Right(_)  => Redirect(routes.TaskController.taskList()).flashing("success" -> "Task added!")
          case Left(err) => Redirect(routes.TaskController.taskList()).flashing("error"   -> err.message)
        }.recover { case _ =>
          Redirect(routes.TaskController.taskList()).flashing("error" -> "Unexpected error while adding task")
        }
    }
  }

  def deleteTask() = Action.async { implicit request =>
    val form     = request.body.asFormUrlEncoded.getOrElse(Map.empty)
    val indexStr = form.get("index").flatMap(_.headOption).getOrElse("").trim

    request.session.get("email") match {
      case None =>
        Future.successful(Unauthorized("Unauthorized"))
      case Some(email) =>
        indexStr.toIntOption match {
          case None =>
            Future.successful(Redirect(routes.TaskController.taskList()).flashing("error" -> "Invalid task index"))
          case Some(index) =>
            taskService.delete(email, index).map {
              case Right(_)  => Redirect(routes.TaskController.taskList()).flashing("success" -> "Task deleted!")
              case Left(err) => Redirect(routes.TaskController.taskList()).flashing("error"   -> err.message)
            }.recover { case _ =>
              Redirect(routes.TaskController.taskList()).flashing("error" -> "Unexpected error while deleting task")
            }
        }
    }
  }
}
