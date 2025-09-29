package security

import javax.inject.Inject
import org.pac4j.core.config.Config
import org.pac4j.core.client.Clients
import org.pac4j.core.credentials.authenticator.Authenticator
import org.pac4j.core.credentials.UsernamePasswordCredentials
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.context.WebContext
import org.pac4j.http.client.indirect.FormClient
import play.cache.SyncCacheApi
import services.IUserService
import scala.concurrent.ExecutionContext
import scala.util.{Success, Failure, Try}

class SecurityModule @Inject()(userService: IUserService, syncCacheApi: SyncCacheApi)(implicit ec: ExecutionContext) {

  // PlaySessionStore instance - make it accessible
  val sessionStore = new org.pac4j.play.store.PlayCacheSessionStore(syncCacheApi)

  // Custom authenticator for database authentication
  class DatabaseAuthenticator extends Authenticator[UsernamePasswordCredentials] {
    override def validate(credentials: UsernamePasswordCredentials, webContext: WebContext): Unit = {
      try {
        val email = credentials.getUsername
        val password = credentials.getPassword

        // For Pac4j 4.5.7, we use the void validate method
        val userFuture = userService.authenticate(email, password)

        // Use Try to handle potential exceptions with longer timeout
        val userResult = Try {
          scala.concurrent.Await.result(userFuture, scala.concurrent.duration.Duration("30 seconds"))
        }

        userResult match {
          case Success(Some(user)) =>
            // Create and populate the profile
            val profile = new CommonProfile()
            profile.setId(user.email)
            profile.addAttribute("email", user.email)
            profile.addAttribute("userId", user.id.toString)
            profile.addAttribute("username", user.email.split("@").head)
            profile.addAttribute("role", user.role.value)

            // Set the profile on credentials - this is crucial for Pac4j
            credentials.setUserProfile(profile)

          case Success(None) =>
            // Authentication failed - Pac4j will handle this
            ()

          case Failure(exception) =>
            println(s"❌ Authentication error for $email: ${exception.getMessage}")
            // For timeout or other errors, we should not set profile
        }
      } catch {
        case e: Exception =>
          println(s"❌ Unexpected error in authenticator: ${e.getMessage}")
        // For void method, exceptions don't return anything
      }
    }
  }

  // Create form client with database authenticator
  val formClient = {
    val client = new FormClient("/login", new DatabaseAuthenticator())
    client.setUsernameParameter("email")
    client.setPasswordParameter("password")
    client
  }

  // Configure pac4j with proper callback URL
  val clients = new Clients("http://localhost:9000/callback", formClient)
  val config = new Config(clients)

  // Set the default clients (Pac4j 4.5.7 API)
  config.setClients(clients)
  
  // CRITICAL: Set defaultSecurityClients (Pac4j 4.5.7 requirement)
  config.getClients.setDefaultSecurityClients(formClient.getName)

  // Get Pac4j config for filters
  def getPac4jConfig: Config = config
}