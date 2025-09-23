package controllers

import javax.inject.Inject
import play.api.mvc._
import security.{SecurityModule, SessionFactory}
import org.pac4j.core.config.Config
import org.pac4j.core.profile.CommonProfile
import org.pac4j.play.{PlayWebContext}
import org.pac4j.play.store.PlaySessionStore
import org.pac4j.core.credentials.UsernamePasswordCredentials
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Try, Success, Failure}

class CallbackController @Inject()(
                                    securityModule: SecurityModule,
                                    sessionFactory: SessionFactory,
                                    val controllerComponents: ControllerComponents,
                                    config: Config,
                                    sessionStore: PlaySessionStore
                                  )(implicit ec: ExecutionContext) extends BaseController {

  def callback() = Action.async { implicit request: Request[AnyContent] =>
    println(s"🔄 CallbackController: Processing Pac4j callback")
    println(s"🔄 CallbackController: Request path: ${request.path}")
    println(s"🔄 CallbackController: Request method: ${request.method}")
    println(s"🔄 CallbackController: Request query string: ${request.queryString}")

    Future {
      try {
        val webContext = new PlayWebContext(request, sessionStore)
        handleFormAuthentication(request, webContext)
      } catch {
        case e: Exception =>
          println(s"❌ CallbackController: Error in callback - ${e.getMessage}")
          e.printStackTrace()
          Redirect(routes.AuthController.login())
            .flashing("error" -> "Authentication failed. Please try again.")
      }
    }
  }

  private def handleFormAuthentication(request: Request[AnyContent], webContext: PlayWebContext): Result = {
    println(s"🔄 CallbackController: Handling form authentication")

    // First try to get credentials from form data
    val formCredentials = request.body.asFormUrlEncoded.flatMap { formData =>
      for {
        emailSeq <- formData.get("email")
        passwordSeq <- formData.get("password")
        email <- emailSeq.headOption
        password <- passwordSeq.headOption
        if email.nonEmpty && password.nonEmpty
      } yield (email, password)
    }

    // If not in form data, try session
    val sessionCredentials = for {
      email <- request.session.get("email")
      password <- request.session.get("password")
      if email.nonEmpty && password.nonEmpty
    } yield (email, password)

    val credentials = formCredentials.orElse(sessionCredentials)

    credentials match {
      case Some((email, password)) =>
        println(s"🔄 CallbackController: Authenticating user: $email")

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
              println(s"✅ CallbackController: Authentication successful for: ${commonProfile.getId}")

              // Save the profile using ProfileManager only
              val profileManager = new org.pac4j.core.profile.ProfileManager[CommonProfile](webContext)
              profileManager.save(true, commonProfile, false)

              println(s"✅ CallbackController: Profile saved successfully")

              // Debug: Try to retrieve the profile immediately to verify it was saved
              val retrievedProfiles = profileManager.getAll(true)
              println(s"🔍 CallbackController: Immediate verification - found ${retrievedProfiles.size()} profiles")

              // Also manually add to Play session as backup
              val sessionData = request.session +
                ("pac4j.userEmail" -> commonProfile.getId) +
                ("pac4j.userRole" -> commonProfile.getAttribute("role").toString)

              println(s"🔍 CallbackController: Added backup session data")

              // Redirect with session data preserved
              Redirect("/taskList").withSession(sessionData)

            case None =>
              println(s"❌ CallbackController: Authentication failed - no profile set")
              Redirect(routes.AuthController.login())
                .flashing("error" -> "Invalid email or password.")
          }
        } catch {
          case e: Exception =>
            println(s"❌ CallbackController: Authentication error: ${e.getMessage}")
            e.printStackTrace()
            Redirect(routes.AuthController.login())
              .flashing("error" -> "Authentication failed. Please try again.")
        }

      case None =>
        println(s"❌ CallbackController: No valid credentials found")
        Redirect(routes.AuthController.login())
          .flashing("error" -> "Please provide both email and password.")
    }
  }
}