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
import org.pac4j.play.PlayWebContext
import org.pac4j.play.store.PlaySessionStore
import scala.concurrent.{ExecutionContext, Future}

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
        println(s"🔄 AuthController: Processing authentication for ${loginData.email}")
        
        userService.authenticate(loginData.email, loginData.password).map {
          case Some(user) =>
            println(s"✅ AuthController: User authenticated successfully: ${user.email}")
            
            // Create Pac4j profile using SessionFactory
            val webContext = sessionFactory.createWebContext(request)
            println(s"🏭 AuthController: Created WebContext using SessionFactory for user: ${user.email}")
            
            val profile = new CommonProfile()
            profile.setId(user.email)
            profile.addAttribute("email", user.email)
            profile.addAttribute("userId", user.id.toString)
            profile.addAttribute("username", user.email.split("@").head)
            profile.addAttribute("role", user.role.value)
            println(s"🏭 AuthController: Created CommonProfile with ID: ${profile.getId}, role: ${profile.getAttribute("role")}")
            
            // Save profile using SessionFactory
            val saveSuccess = sessionFactory.saveProfile(webContext, profile)
            if (saveSuccess) {
              println(s"✅ AuthController: Profile saved successfully using SessionFactory for user: ${user.email}")
            } else {
              println(s"❌ AuthController: Failed to save profile using SessionFactory for user: ${user.email}")
            }
            
            // Also save profile data directly to Play session as backup
            val profileData = Map(
              "profileId" -> profile.getId,
              "profileEmail" -> profile.getAttribute("email").toString,
              "profileRole" -> profile.getAttribute("role").toString,
              "profileUserId" -> profile.getAttribute("userId").toString
            )
            println(s"🔧 AuthController: Storing profile data in Play session: $profileData")
            
            // Store all data in session including profile data
            val session = request.session ++ Map(
              "email" -> user.email,
              "userId" -> user.id.toString,
              "username" -> user.email.split("@").head,
              "role" -> user.role.value
            ) ++ profileData
            
            Redirect(routes.TaskController.taskList())
              .withSession(session)
              .flashing("success" -> "Login successful!")
          case None =>
            println(s"❌ AuthController: Authentication failed for: ${loginData.email}")
            Redirect(routes.AuthController.login())
              .flashing("error" -> "Invalid email or password.")
        }.recover { case e =>
          println(s"❌ AuthController: Authentication error: ${e.getMessage}")
          Redirect(routes.AuthController.login())
            .flashing("error" -> "Authentication error.")
        }
      }
    )
  }

  def logout() = Action.async { implicit request =>
    Future {
      try {
        println(s"🔄 AuthController: Processing Pac4j logout")
        
        val webContext = new PlayWebContext(request, sessionStore)
        
        // Clear Pac4j session
        val profileManager = new org.pac4j.core.profile.ProfileManager(webContext, sessionStore)
        profileManager.remove(true)
        
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
