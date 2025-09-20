package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import services.UserService
import models.{User, Role}
import scala.concurrent.{ExecutionContext, Future}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents, userService: UserService)(implicit ec: ExecutionContext) extends BaseController {

  def index() = Action { implicit request: Request[AnyContent] =>
    // Eğer kullanıcı giriş yapmışsa task listesine yönlendir
    request.session.get("email") match {
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
    Redirect(routes.AuthController.login()).withNewSession
  }

  def adminDashboard() = Action.async { implicit request: Request[AnyContent] =>
    // SecurityFilter ensures admin access, so we can directly get users
    userService.list().map { users =>
      Ok(views.html.admin.dashboard(users))
    }
  }

}
