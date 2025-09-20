package controllers

import javax.inject.Inject
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.Results
import play.api.Configuration
import services.IUserService
import security.SecurityModule
import org.pac4j.core.profile.CommonProfile
import scala.concurrent.{ExecutionContext, Future}

class AuthController @Inject()(
  userService: IUserService,
  securityModule: SecurityModule,
  val controllerComponents: ControllerComponents
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
    Ok(views.html.login())
  }

  def authenticate() = Action.async { implicit request =>
    loginForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(
          Redirect(routes.AuthController.login())
            .flashing("error" -> "Please enter valid email and password.")
        )
      },
      loginData => {
        println(s"Pac4j AuthController: Starting authentication for ${loginData.email}")
        
        userService.authenticate(loginData.email, loginData.password).map {
          case Some(user) =>
            println(s"Pac4j AuthController: User found in database: ${user.email}")
            
            // Create Pac4j profile
            val profile = securityModule.createProfile(user.email, user.id.toString, user.email.split("@").head)
            
            // Generate JWT token using Pac4j
            val token = securityModule.generateJwtToken(profile)
            
            println(s"Pac4j AuthController: JWT token generated and stored in session")
            
            // Store in session and redirect to task list
            Redirect(routes.TaskController.taskList())
              .withSession(
                "email" -> user.email,
                "userId" -> user.id.toString,
                "jwtToken" -> token
              )
              .flashing("success" -> "Login successful with Pac4j!")
          case None =>
            println(s"Pac4j AuthController: User not found in database: ${loginData.email}")
            Redirect(routes.AuthController.login())
              .flashing("error" -> "Invalid email or password.")
        }.recover { case e =>
          println(s"Pac4j AuthController: Authentication error: ${e.getMessage}")
          Redirect(routes.AuthController.login())
            .flashing("error" -> "Authentication error.")
        }
      }
    )
  }

  def logout() = Action { implicit request =>
    Redirect(routes.AuthController.login())
      .withNewSession
      .flashing("success" -> "You have been logged out.")
  }

  // Pac4j JWT token validation endpoint
  def validateToken() = Action.async { implicit request =>
    request.headers.get("Authorization") match {
      case Some(authHeader) if authHeader.startsWith("Bearer ") =>
        val token = authHeader.substring(7)
        // For now, just return the token info (simplified validation)
        Future.successful(Ok(s"Pac4j JWT Token: ${token.take(50)}..."))
      case _ =>
        Future.successful(Unauthorized("No token provided"))
    }
  }

  // Get user info from Pac4j JWT token
  def getUserInfo() = Action.async { implicit request =>
    request.headers.get("Authorization") match {
      case Some(authHeader) if authHeader.startsWith("Bearer ") =>
        val token = authHeader.substring(7)
        // For now, return session info (simplified)
        val email = request.session.get("email").getOrElse("Unknown")
        Future.successful(Ok(s"Pac4j User: $email"))
      case _ =>
        Future.successful(Unauthorized("No token provided"))
    }
  }
}
