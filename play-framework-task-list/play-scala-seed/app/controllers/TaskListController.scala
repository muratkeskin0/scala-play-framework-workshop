package controllers

import javax.inject._
import play.api._
import play.api.mvc._

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
  def taskList() = Action {
    val tasks = List("code","sleep","eat")
    Ok(views.html.taskList(tasks))
  }

  def signUpValidate() = Action { request =>
    val postVals = request.body.asFormUrlEncoded
    postVals.map { args =>
      val username = args("username").head
      val password = args("password").head
      //Ok(s" $username signed up with password : $password")
      Redirect(routes.TaskListController.taskList())
    }.getOrElse(Ok("An Error Has Occured"))
  }


}
