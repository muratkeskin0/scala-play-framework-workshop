package filters

import javax.inject.Inject
import play.api.mvc._
import play.api.Configuration
import security.{SecurityModule, SessionFactory}
import models.Role
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.streams.Accumulator
import akka.util.ByteString
import org.pac4j.core.config.Config
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.ProfileManager
import org.pac4j.play.PlayWebContext
import org.pac4j.play.store.PlaySessionStore

class Pac4jSecurityFilter @Inject()(
  securityModule: SecurityModule,
  sessionFactory: SessionFactory,
  configuration: Configuration,
  sessionStore: PlaySessionStore
)(implicit ec: ExecutionContext) extends EssentialFilter {

  // Get Pac4j configuration
  private val pac4jConfig = securityModule.getPac4jConfig
  
  // Security logic is implemented using SessionFactory
  
  // Public routes that don't require authentication
  private val publicPaths = Set(
    "/login",
    "/signUp", 
    "/signUpValidate",
    "/loginValidate",
    "/assets/",
    "/callback"
  )
  
  // Admin routes that require admin role
  private val adminPaths = Set(
    "/admin/dashboard",
    "/admin/users/update",
    "/admin/users/delete"
  )

  override def apply(next: EssentialAction): EssentialAction = {
    EssentialAction { request =>
      val path = request.path
      
      // Check if route is public
      if (isPublicPath(path)) {
        next(request)
      } else {
        // Use Pac4j security logic
        handleSecurityWithPac4j(request, next)
      }
    }
  }
  
  private def isPublicPath(path: String): Boolean = {
    publicPaths.exists(route => path.startsWith(route))
  }
  
  private def handleSecurityWithPac4j(request: RequestHeader, next: EssentialAction): Accumulator[ByteString, Result] = {
    try {
      // Create Pac4j web context using SessionFactory
      val webContext = sessionFactory.createWebContext(request)
      println(s"Pac4jSecurityFilter: Created WebContext using SessionFactory for path: ${request.path}")
      
      // Get current user profile using SessionFactory
      val profiles = sessionFactory.getProfiles(webContext)
      println(s"Pac4jSecurityFilter: Retrieved ${profiles.size} profiles using SessionFactory for path: ${request.path}")
      
      // Also check Play session for profile data as fallback
      val sessionEmail = request.session.get("email")
      val sessionProfileId = request.session.get("profileId")
      println(s"Pac4jSecurityFilter: Play session email: $sessionEmail, profileId: $sessionProfileId")
      
      if (profiles.isEmpty && sessionEmail.isEmpty) {
        // No authenticated user - redirect to login
        println(s"Pac4jSecurityFilter: No Pac4j profile or session found for path: ${request.path}")
        Accumulator.done(
          Results.Redirect(controllers.routes.AuthController.login())
            .flashing("error" -> "Please login to access this page")
        )
      } else {
        // Use Pac4j profile if available, otherwise use session data
        if (!profiles.isEmpty) {
          val profile = profiles.get(0).asInstanceOf[CommonProfile]
          println(s"Pac4jSecurityFilter: Using Pac4j profile for user: ${profile.getId}")
          
          // Check if admin route requires admin role
          if (isAdminPath(request.path)) {
            val userRole = profile.getAttribute("role").asInstanceOf[String]
            if (userRole == null || userRole != "admin") {
              println(s"Pac4jSecurityFilter: Access denied for user: ${profile.getId}, role: ${userRole}")
              Accumulator.done(
                Results.Redirect(controllers.routes.TaskController.taskList())
                  .flashing("error" -> "Access denied. Admin privileges required.")
              )
            } else {
              println(s"Pac4jSecurityFilter: Admin access granted for user: ${profile.getId}")
              next(request)
            }
          } else {
            // Regular authenticated route
            println(s"Pac4jSecurityFilter: Access granted for user: ${profile.getId}")
            next(request)
          }
        } else {
          // Fallback to session data
          val sessionRole = request.session.get("role").getOrElse("")
          println(s"Pac4jSecurityFilter: Using session data for user: $sessionEmail, role: $sessionRole")
          
          // Check if admin route requires admin role
          if (isAdminPath(request.path)) {
            if (sessionRole != "admin") {
              println(s"Pac4jSecurityFilter: Access denied for user: $sessionEmail, role: $sessionRole")
              Accumulator.done(
                Results.Redirect(controllers.routes.TaskController.taskList())
                  .flashing("error" -> "Access denied. Admin privileges required.")
              )
            } else {
              println(s"Pac4jSecurityFilter: Admin access granted for user: $sessionEmail")
              next(request)
            }
          } else {
            // Regular authenticated route
            println(s"Pac4jSecurityFilter: Access granted for user: $sessionEmail")
            next(request)
          }
        }
      }
    } catch {
      case e: Exception =>
        println(s"Pac4jSecurityFilter Error: ${e.getMessage}")
        e.printStackTrace()
        Accumulator.done(
          Results.Redirect(controllers.routes.AuthController.login())
            .flashing("error" -> "Authentication error")
        )
    }
  }
  
  private def isAdminPath(path: String): Boolean = {
    adminPaths.exists(route => path.startsWith(route))
  }
}
