package controllers

import javax.inject.Inject
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.Results
import play.api.Configuration
import play.api.libs.json.Json
import services.IUserService
import security.{SecurityModule, SessionFactory}
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.config.Config
import org.pac4j.play.{PlayWebContext}
import org.pac4j.play.store.PlaySessionStore
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

class AuthController @Inject()(
                                userService: IUserService,
                                securityModule: SecurityModule,
                                sessionFactory: SessionFactory,
                                val controllerComponents: ControllerComponents,
                                config: Config,
                                sessionStore: PlaySessionStore
                              )(implicit ec: ExecutionContext) extends BaseController {

  // Login form
  val loginForm = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText
    )(LoginData.apply)(LoginData.unapply)
  )

  case class LoginData(email: String, password: String)

  def login() = Action { implicit request =>
    println(s"🔄 AuthController: Serving login page")
    Ok(views.html.login())
  }

  def authenticate() = Action.async { implicit request =>
    println(s"🔄 AuthController: authenticate() called")
    println(s"🔄 AuthController: Request path: ${request.path}")
    println(s"🔄 AuthController: Request method: ${request.method}")
    println(s"🔄 AuthController: Request body: ${request.body}")

    // Extract form data to ensure it's present
    request.body.asFormUrlEncoded match {
      case Some(formData) =>
        println(s"🔄 AuthController: Form data received: ${formData.keys}")

        // Store credentials in session and redirect to callback
        val email = formData.getOrElse("email", List("")).head
        val password = formData.getOrElse("password", List("")).head

        Future.successful(Redirect(routes.CallbackController.callback()).withSession(
          request.session +
            ("email" -> email) +
            ("password" -> password)
        ))

      case None =>
        println(s"❌ AuthController: No form data received")
        Future.successful(Redirect(routes.AuthController.login())
          .flashing("error" -> "Please fill in all fields"))
    }
  }

  def logout() = Action.async { implicit request =>
    Future {
      try {
        println(s"🔄 AuthController: Processing logout")

        val webContext = new PlayWebContext(request, sessionStore)

        // Clear Pac4j session
        val profileManager = new org.pac4j.core.profile.ProfileManager(webContext)
        profileManager.remove(true)

        // Also clear session store directly
        sessionStore.destroySession(webContext)

        println(s"✅ AuthController: Logout processed successfully")
        Redirect(routes.AuthController.login())
          .withNewSession
          .flashing("success" -> "You have been logged out.")
      } catch {
        case e: Exception =>
          println(s"❌ AuthController: Logout error - ${e.getMessage}")
          Redirect(routes.AuthController.login())
            .withNewSession
            .flashing("success" -> "You have been logged out.")
      }
    }
  }
}