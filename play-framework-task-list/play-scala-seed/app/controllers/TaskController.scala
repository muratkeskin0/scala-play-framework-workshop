package controllers

import javax.inject._
import play.api.mvc._
import services.ITaskService

@Singleton
class TaskController @Inject()( val controllerComponents: ControllerComponents,val _taskService: ITaskService) extends BaseController {

  def taskList() = Action { implicit request =>
    request.session.get("username").map { username =>
      val tasks = _taskService.list(username)
      Ok(views.html.taskList(tasks))
    }.getOrElse(Unauthorized("Unauthorized"))
  }

  def addTask() = Action { implicit request =>
    val form    = request.body.asFormUrlEncoded.getOrElse(Map.empty)
    val rawTask = form.get("task").flatMap(_.headOption).getOrElse("")

    request.session.get("username") match {
      case None => Unauthorized("Unauthorized")
      case Some(username) =>
        _taskService.add(username, rawTask) match {
          case Right(_)  => Redirect(routes.TaskController.taskList()).flashing("success" -> "Task added!")
          case Left(err) => Redirect(routes.TaskController.taskList()).flashing("error"   -> err.message)
        }
    }
  }

  def deleteTask() = Action { implicit request =>
    val form     = request.body.asFormUrlEncoded.getOrElse(Map.empty)
    val indexStr = form.get("index").flatMap(_.headOption).getOrElse("")

    request.session.get("username") match {
      case None => Unauthorized("Unauthorized")
      case Some(username) =>
        val index = indexStr.toIntOption.getOrElse(-1)
        _taskService.delete(username, index) match {
          case Right(_)  => Redirect(routes.TaskController.taskList()).flashing("success" -> "Task deleted!")
          case Left(err) => Redirect(routes.TaskController.taskList()).flashing("error"   -> err.message)
        }
    }
  }
}
