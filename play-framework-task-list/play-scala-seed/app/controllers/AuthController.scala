package controllers

import javax.inject.Inject
import play.api.mvc._
import services.IUserService
import security.{SecurityModule, SessionFactory}
import org.pac4j.play.PlayWebContext
import org.pac4j.play.store.PlaySessionStore
import scala.concurrent.{ExecutionContext, Future}

class AuthController @Inject()(
                                userService: IUserService,
                                securityModule: SecurityModule,
                                sessionFactory: SessionFactory,
                                val controllerComponents: ControllerComponents,
                                sessionStore: PlaySessionStore
                              )(implicit ec: ExecutionContext) extends BaseController {


  def login() = Action { implicit request =>
    Ok(views.html.login())
  }

  def authenticate() = Action.async { implicit request =>
    // Extract form data to ensure it's present
    request.body.asFormUrlEncoded match {
      case Some(formData) =>
        // Store credentials in session and redirect to callback
        val email = formData.getOrElse("email", List("")).head
        val password = formData.getOrElse("password", List("")).head

        Future.successful(Redirect("/callback").withSession(
          request.session +
            ("email" -> email) +
            ("password" -> password)
        ))

      case None =>
        Future.successful(Redirect(routes.AuthController.login())
          .flashing("error" -> "Please fill in all fields"))
    }
  }

  def logout() = Action.async { implicit request =>
    Future {
      try {

        val webContext = new PlayWebContext(request, sessionStore)

        // Clear Pac4j session
        val profileManager = new org.pac4j.core.profile.ProfileManager(webContext)
        profileManager.remove(true)

        // Also clear session store directly
        sessionStore.destroySession(webContext)

        Redirect(routes.AuthController.login())
          .withNewSession
          .flashing("success" -> "You have been logged out.")
      } catch {
        case e: Exception =>
          println(s"❌ Logout error - ${e.getMessage}")
          Redirect(routes.AuthController.login())
            .withNewSession
            .flashing("success" -> "You have been logged out.")
      }
    }
  }
}