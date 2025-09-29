package controllers

import javax.inject._
import play.api.mvc._
import services.UserService
import models.{User, Role}
import scala.concurrent.{ExecutionContext, Future}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents, userService: UserService, sessionFactory: security.SessionFactory, securityModule: security.SecurityModule)(implicit ec: ExecutionContext) extends BaseController {

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
    // Manual authentication check for admin dashboard
    checkAdminAccess(request).flatMap {
      case Some(user) if user.role == Role.Admin =>
        userService.list().map { users =>
          Ok(views.html.admin.dashboard(users))
        }
      case Some(_) =>
        Future.successful(Redirect(routes.AuthController.login())
          .flashing("error" -> "Access denied. Admin privileges required."))
      case None =>
        Future.successful(Redirect(routes.AuthController.login())
          .flashing("error" -> "Please login to access admin dashboard."))
    }
  }

  // Helper method to check admin access
  private def checkAdminAccess(request: Request[AnyContent]): Future[Option[User]] = {
    try {
      // Try to get profile from session store
      val webContext = sessionFactory.createWebContext(request)
      val sessionStore = securityModule.sessionStore
      val profileOption = sessionStore.get(webContext, "pac4j_profiles")
      
      if (profileOption.isPresent) {
        val profile = profileOption.get().asInstanceOf[org.pac4j.core.profile.CommonProfile]
        val email = profile.getId
        val role = profile.getAttribute("role").toString
        
        if (role == "admin") {
          userService.get(email)
        } else {
          Future.successful(None)
        }
      } else {
        // Fallback to session data
        val sessionEmail = request.session.get("pac4j.userEmail")
        val sessionRole = request.session.get("pac4j.userRole")
        
        (sessionEmail, sessionRole) match {
          case (Some(email), Some(role)) if role == "admin" =>
            userService.get(email)
          case _ =>
            Future.successful(None)
        }
      }
    } catch {
      case e: Exception =>
        println(s"❌ Error checking admin access: ${e.getMessage}")
        Future.successful(None)
    }
  }

  def testDatabase() = Action.async { implicit request: Request[AnyContent] =>
    val startTime = System.currentTimeMillis()
    userService.list().map { users =>
      val endTime = System.currentTimeMillis()
      Ok(s"Database test successful! Found ${users.length} users in ${endTime - startTime}ms")
    }.recover {
      case e: Exception =>
        val endTime = System.currentTimeMillis()
        Ok(s"Database test failed after ${endTime - startTime}ms: ${e.getMessage}")
    }
  }

}
