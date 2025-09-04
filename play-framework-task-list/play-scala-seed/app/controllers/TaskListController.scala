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
  def taskList() = Action { request =>
    //val tasks = List("code","sleep","eat")  hard coded list
    val usernameOption =  request.session.get("username")
    usernameOption.map { username =>
      val tasks = TaskListInMemoryModel.getAllTasks(username)
      Ok(views.html.taskList(tasks))
    }.getOrElse(Ok("UnAuthorized User"))
  }

  def signUpValidate() = Action { request =>
    val postVals = request.body.asFormUrlEncoded
    postVals.map { args =>
      val username = args("username").head
      val password = args("password").head
      //Ok(s" $username signed up with password : $password")
      if(TaskListInMemoryModel.createUser(username,password)) {
        Redirect(routes.TaskListController.taskList()).withSession("username" -> username)
      }
      else{
        Ok("Error happened")
      }
    }.getOrElse(Ok("An Error Has Occured"))
  }

  def loginValidate() = Action { request =>
    val postVals = request.body.asFormUrlEncoded
    postVals.map { args =>
      val username = args("username").head
      val password = args("password").head
      //Ok(s" $username signed up with password : $password")
      if(TaskListInMemoryModel.validateUser(username,password)) {
        Redirect(routes.TaskListController.taskList()).withSession("username" -> username)
      }
      else{
        Ok("Error happened")
      }
    }.getOrElse(Ok("An Error Has Occured"))
  }




}
