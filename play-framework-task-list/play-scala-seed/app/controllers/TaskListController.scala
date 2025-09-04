package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import models.TaskListInMemoryModel

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class TaskListController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def taskList() = Action { implicit request =>
    //val tasks = List("code","sleep","eat")  hard coded list
    val usernameOption =  request.session.get("username")
    usernameOption.map { username =>
      val tasks = TaskListInMemoryModel.getAllTasks(username)
      Ok(views.html.taskList(tasks))
    }.getOrElse(Ok("UnAuthorized User"))
  }

  def signUpValidate() = Action { implicit request =>
    val postVals = request.body.asFormUrlEncoded
    postVals.map { args =>
      val username = args("username").head
      val password = args("password").head
      //Ok(s" $username signed up with password : $password")
      if(TaskListInMemoryModel.createUser(username,password)) {
        Redirect(routes.TaskListController.taskList()).withSession("username" -> username)
      }
      else{
        Ok("error")
      }
    }.getOrElse(Ok("An Error Has Occured"))
  }

  def loginValidate() = Action { implicit request =>
    val postVals = request.body.asFormUrlEncoded
    postVals.map { args =>
      val username = args("username").head
      val password = args("password").head
      //Ok(s" $username signed up with password : $password")
      if(TaskListInMemoryModel.validateUser(username,password)) {
        Redirect(routes.TaskListController.taskList()).withSession("username" -> username)
      }
      else{
        Redirect(routes.HomeController.signUp()).flashing("error" -> "error happened when you were trying to log in")
      }
    }.getOrElse(Ok("An Error Has Occured"))
  }

  def addTask() = Action { implicit request =>
    val form     = request.body.asFormUrlEncoded.getOrElse(Map.empty)
    val taskOpt  = form.get("task").flatMap(_.headOption).map(_.trim)
    val userOpt  = request.session.get("username")

    (userOpt, taskOpt) match {
      case (Some(username), Some(task)) if task.nonEmpty =>
        val ok = TaskListInMemoryModel.addTask(username, task)
        if (ok)
          Redirect(routes.TaskListController.taskList()).flashing("success" -> "Task added!")
        else
          Redirect(routes.TaskListController.taskList()).flashing("error" -> "Could not add task.")
      case (Some(_), _) =>
        Redirect(routes.TaskListController.taskList()).flashing("error" -> "Please enter a task.")
      case _ =>
        Unauthorized("Unauthorized")
    }
  }

  def deleteTask() = Action { implicit request =>
    val form     = request.body.asFormUrlEncoded.getOrElse(Map.empty)
    val userOpt  = request.session.get("username")
    val indexOpt = form.get("index").flatMap(_.headOption).flatMap(_.trim.toIntOption)

    (userOpt, indexOpt) match {
      case (Some(username), Some(i)) => {
        val ok = TaskListInMemoryModel.deleteTask(username, i)
        if (ok)
          Redirect(routes.TaskListController.taskList()).flashing("success" -> "Task deleted!")
        else
          Redirect(routes.TaskListController.taskList()).flashing("error" -> "Could not delete task.")
      }

      case (Some(_), None) =>
        Redirect(routes.TaskListController.taskList()).flashing("error" -> "Please choose a valid task to delete.")

      case _ =>
        Unauthorized("Unauthorized")
    }

  }





}
