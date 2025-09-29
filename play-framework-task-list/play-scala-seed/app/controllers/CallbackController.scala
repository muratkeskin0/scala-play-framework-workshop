package controllers

import javax.inject.Inject
import play.api.mvc._
import security.{SecurityModule, SessionFactory}
import org.pac4j.core.profile.CommonProfile
import org.pac4j.play.PlayWebContext
import org.pac4j.core.credentials.UsernamePasswordCredentials
import scala.concurrent.{ExecutionContext, Future}

class CallbackController @Inject()(
                                    securityModule: SecurityModule,
                                    sessionFactory: SessionFactory,
                                    val controllerComponents: ControllerComponents
                                  )(implicit ec: ExecutionContext) extends BaseController {

  def callback() = Action.async { implicit request: Request[AnyContent] =>
    Future {
      try {
        val webContext = sessionFactory.createWebContext(request)
        handleFormAuthentication(request, webContext)
      } catch {
        case e: Exception =>
          println(s"❌ Callback error: ${e.getMessage}")
          Redirect(routes.AuthController.login())
            .flashing("error" -> "Authentication failed. Please try again.")
      }
    }
  }

  private def handleFormAuthentication(request: Request[AnyContent], webContext: PlayWebContext): Result = {
    // Get credentials from session
    val sessionCredentials = for {
      email <- request.session.get("email")
      password <- request.session.get("password")
      if email.nonEmpty && password.nonEmpty
    } yield (email, password)

    sessionCredentials match {
      case Some((email, password)) =>
        // Create credentials and authenticate using our DatabaseAuthenticator
        val usernamePasswordCredentials = new UsernamePasswordCredentials(email, password)
        val authenticator = new securityModule.DatabaseAuthenticator()

        try {
          // Call the authenticator (void method in Pac4j 4.5.7)
          authenticator.validate(usernamePasswordCredentials, webContext)

          // Check if profile was set on credentials
          val profileOption = Option(usernamePasswordCredentials.getUserProfile)

          profileOption match {
            case Some(profile) =>
              val commonProfile = profile.asInstanceOf[CommonProfile]

              // WORKAROUND: Pac4j 4.5.7 ProfileManager bug - use direct session store access
              val sessionStore = securityModule.sessionStore
              
              // Clear any existing profiles first
              sessionStore.set(webContext, "pac4j_profiles", null)
              
              // Save the profile directly to session store
              sessionStore.set(webContext, "pac4j_profiles", commonProfile)
              
              // Verify profile was saved
              val savedProfile = sessionStore.get(webContext, "pac4j_profiles")
              
              if (savedProfile.isPresent) {
                // Also save to Play session as backup
                val sessionData = request.session +
                  ("pac4j.userEmail" -> commonProfile.getId) +
                  ("pac4j.userRole" -> commonProfile.getAttribute("role").toString) +
                  ("email" -> commonProfile.getId) // Add email for fallback

                // Redirect to protected URL to trigger SecurityFilter
                Redirect("/taskList").withSession(sessionData)
              } else {
                Redirect(routes.AuthController.login())
                  .flashing("error" -> "Profile save failed. Please try again.")
              }

            case None =>
              Redirect(routes.AuthController.login())
                .flashing("error" -> "Invalid email or password.")
          }
        } catch {
          case e: Exception =>
            println(s"❌ Authentication error: ${e.getMessage}")
            Redirect(routes.AuthController.login())
              .flashing("error" -> "Authentication failed. Please try again.")
        }

      case None =>
        Redirect(routes.AuthController.login())
          .flashing("error" -> "Please provide both email and password.")
    }
  }
}
