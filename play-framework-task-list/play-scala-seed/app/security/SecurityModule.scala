package security

import javax.inject.Inject
import org.pac4j.core.config.Config
import org.pac4j.core.client.Clients
import org.pac4j.core.credentials.authenticator.Authenticator
import org.pac4j.core.credentials.UsernamePasswordCredentials
import org.pac4j.core.profile.CommonProfile
import org.pac4j.http.client.indirect.FormClient
import play.api.Configuration
import play.api.mvc.{Request, RequestHeader, Result, Results}
import services.IUserService
import models.{User, Role}
import controllers.routes
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure}
import java.util.Optional

class SecurityModule @Inject()(userService: IUserService, configuration: Configuration)(implicit ec: ExecutionContext) {

  // Custom authenticator for database authentication
  class DatabaseAuthenticator extends Authenticator[UsernamePasswordCredentials] {
    override def validate(credentials: UsernamePasswordCredentials, webContext: org.pac4j.core.context.WebContext): Unit = {
      credentials match {
        case upc: UsernamePasswordCredentials =>
          val email = upc.getUsername
          val password = upc.getPassword
          
          println(s"🔐 SecurityModule: Validating credentials for email: $email")
          
          // Synchronous authentication for Pac4j
          try {
            val userFuture = userService.authenticate(email, password)
            val user = scala.concurrent.Await.result(userFuture, scala.concurrent.duration.Duration.Inf)
            
            user match {
              case Some(u) =>
                println(s"✅ SecurityModule: User authenticated successfully: ${u.email}")
                val profile = new CommonProfile()
                profile.setId(u.email)
                profile.addAttribute("email", u.email)
                profile.addAttribute("userId", u.id.toString)
                profile.addAttribute("username", u.email.split("@").head)
                profile.addAttribute("role", u.role.value)
                upc.setUserProfile(profile)
              case None =>
                println(s"❌ SecurityModule: Authentication failed for email: $email")
                // Invalid credentials - do nothing, Pac4j will handle the failure
            }
          } catch {
            case e: Exception =>
              println(s"❌ SecurityModule: Authentication error for email: $email - ${e.getMessage}")
              // Authentication error - do nothing
          }
        case _ =>
          println(s"❌ SecurityModule: Invalid credentials type")
          // Invalid credentials type - do nothing
      }
    }
  }

  // Create form client with database authenticator
  val formClient = new FormClient("/login", new DatabaseAuthenticator())

  // Configure pac4j
  val config = new Config(new Clients(formClient))
  
  // Get Pac4j config for filters
  def getPac4jConfig: Config = config
}