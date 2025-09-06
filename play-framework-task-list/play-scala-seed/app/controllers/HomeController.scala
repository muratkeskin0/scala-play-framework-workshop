package controllers

import javax.inject._
import play.api._
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  def index() = Action { implicit request: Request[AnyContent] =>
    // Eğer kullanıcı giriş yapmışsa task listesine yönlendir
    request.session.get("username") match {
      case Some(_) => Redirect(routes.TaskController.taskList())
      case None => Ok(views.html.index())
    }
  }

  def todo() = TODO;

  def signUp() = Action {implicit request =>
    Ok(views.html.signUp())
  }

  def login() = Action { implicit request =>
    Ok(views.html.login())
  }

  def logout() = Action {
    Redirect(routes.HomeController.login()).withNewSession
  }

}
