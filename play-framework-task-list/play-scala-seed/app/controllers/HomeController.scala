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
    // Check if user is logged in and is admin
    request.session.get("email") match {
      case Some(email) =>
        userService.get(email).map {
          case Some(user) if user.role == Role.Admin =>
            // Get all users for admin dashboard
            userService.list().map { users =>
              Ok(views.html.admin.dashboard(users))
            }
          case _ =>
            // User not found or not admin - redirect to task list
            Future.successful(Redirect(routes.TaskController.taskList()).flashing("error" -> "Access denied. Admin privileges required."))
        }.flatten
      case None =>
        Future.successful(Redirect(routes.AuthController.login()).flashing("error" -> "Please log in to access this page."))
    }
  }

}
