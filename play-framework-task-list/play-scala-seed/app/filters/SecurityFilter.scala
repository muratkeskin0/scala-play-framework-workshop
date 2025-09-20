package filters

import javax.inject.Inject
import play.api.mvc._
import play.api.Configuration
import security.SecurityModule
import models.Role
import scala.concurrent.{ExecutionContext, Future}

class SecurityFilter @Inject()(
  securityModule: SecurityModule,
  configuration: Configuration
)(implicit ec: ExecutionContext) extends EssentialFilter {

  // Protected routes configuration
  private val protectedRoutes = Set(
    "/taskList",
    "/profile",
    "/addTask",
    "/updateTask", 
    "/deleteTask",
    "/updateProfile",
    "/changePassword"
  )
  
  // Admin-only routes
  private val adminRoutes = Set(
    "/admin/dashboard",
    "/admin/users/update",
    "/admin/users/delete"
  )
  
  // Public routes (no authentication required)
  private val publicRoutes = Set(
    "/",
    "/login",
    "/signUp",
    "/signUpValidate",
    "/loginValidate",
    "/logout",
    "/assets/",
    "/api/validate-token",
    "/api/user-info"
  )

  override def apply(next: EssentialAction): EssentialAction = {
    EssentialAction { request =>
      val path = request.path
      
      // Check if route is public
      if (isPublicRoute(path)) {
        next(request)
      }
      // Check if route requires admin access
      else if (isAdminRoute(path)) {
        handleAdminRoute(request, next)
      }
      // Check if route requires authentication
      else if (isProtectedRoute(path)) {
        handleProtectedRoute(request, next)
      }
      // Default: allow access
      else {
        next(request)
      }
    }
  }
  
  private def isPublicRoute(path: String): Boolean = {
    publicRoutes.exists(route => path.startsWith(route))
  }
  
  private def isAdminRoute(path: String): Boolean = {
    adminRoutes.exists(route => path.startsWith(route))
  }
  
  private def isProtectedRoute(path: String): Boolean = {
    protectedRoutes.exists(route => path.startsWith(route))
  }
  
  private def handleProtectedRoute(request: RequestHeader, next: EssentialAction): play.api.libs.streams.Accumulator[akka.util.ByteString, play.api.mvc.Result] = {
    // Check if user is authenticated via session
    request.session.get("email") match {
      case Some(_) => next(request)
      case None => 
        play.api.libs.streams.Accumulator.done(
          Results.Redirect(controllers.routes.AuthController.login())
            .flashing("error" -> "Please login to access this page")
        )
    }
  }
  
  private def handleAdminRoute(request: RequestHeader, next: EssentialAction): play.api.libs.streams.Accumulator[akka.util.ByteString, play.api.mvc.Result] = {
    // Check if user is authenticated and is admin
    request.session.get("email") match {
      case Some(email) =>
        // For now, just check if user exists and is admin synchronously
        // This is a simplified approach to avoid Materializer complexity
        val isAdmin = try {
          import scala.concurrent.Await
          import scala.concurrent.duration._
          val userFuture = securityModule.getCurrentUserFromSession(request)
          val user = Await.result(userFuture, 1.second)
          user.exists(_.role == Role.Admin)
        } catch {
          case _: Exception => false
        }
        
        if (isAdmin) {
          next(request)
        } else {
          play.api.libs.streams.Accumulator.done(
            Results.Redirect(controllers.routes.TaskController.taskList())
              .flashing("error" -> "Access denied. Admin privileges required.")
          )
        }
      case None =>
        play.api.libs.streams.Accumulator.done(
          Results.Redirect(controllers.routes.AuthController.login())
            .flashing("error" -> "Please login to access this page")
        )
    }
  }
}
