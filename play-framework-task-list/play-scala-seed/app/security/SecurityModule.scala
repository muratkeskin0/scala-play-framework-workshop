package security

import javax.inject.Inject
import org.pac4j.core.config.Config
import org.pac4j.core.client.Clients
import org.pac4j.core.credentials.authenticator.Authenticator
import org.pac4j.core.credentials.UsernamePasswordCredentials
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.context.WebContext
import org.pac4j.http.client.indirect.FormClient
import play.api.Configuration
import play.api.mvc.{Request, RequestHeader, Result, Results}
import play.cache.SyncCacheApi
import services.IUserService
import models.{User, Role}
import controllers.routes
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure, Try}
import java.util.Optional
import scala.jdk.OptionConverters._

class SecurityModule @Inject()(userService: IUserService, configuration: Configuration, syncCacheApi: SyncCacheApi)(implicit ec: ExecutionContext) {

  // PlaySessionStore instance
  private val sessionStore = new org.pac4j.play.store.PlayCacheSessionStore(syncCacheApi)

  def getPlaySessionStore: org.pac4j.play.store.PlaySessionStore = sessionStore

  // Custom authenticator for database authentication
  class DatabaseAuthenticator extends Authenticator[UsernamePasswordCredentials] {
    override def validate(credentials: UsernamePasswordCredentials, webContext: WebContext): Unit = {
      println(s"🔐 SecurityModule: DatabaseAuthenticator.validate() called")
      println(s"🔐 SecurityModule: Credentials type: ${credentials.getClass.getSimpleName}")

      try {
        val email = credentials.getUsername
        val password = credentials.getPassword

        println(s"🔐 SecurityModule: Validating credentials for email: $email")
        println(s"🔐 SecurityModule: Password length: ${password.length}")

        // For Pac4j 4.5.7, we use the void validate method
        val userFuture = userService.authenticate(email, password)

        // Use Try to handle potential exceptions
        val userResult = Try {
          scala.concurrent.Await.result(userFuture, scala.concurrent.duration.Duration("10 seconds"))
        }

        userResult match {
          case Success(Some(user)) =>
            println(s"✅ SecurityModule: User authenticated successfully: ${user.email}")

            // Create and populate the profile
            val profile = new CommonProfile()
            profile.setId(user.email)
            profile.addAttribute("email", user.email)
            profile.addAttribute("userId", user.id.toString)
            profile.addAttribute("username", user.email.split("@").head)
            profile.addAttribute("role", user.role.value)

            println(s"🔐 SecurityModule: Created profile with ID: ${profile.getId}")
            println(s"🔐 SecurityModule: Profile attributes: email=${profile.getAttribute("email")}, role=${profile.getAttribute("role")}")

            // Set the profile on credentials - this is crucial for Pac4j
            credentials.setUserProfile(profile)
            println(s"🔐 SecurityModule: Profile set on credentials")

          case Success(None) =>
            println(s"❌ SecurityModule: Authentication failed for email: $email")
          // For void method, we don't set profile on failure - Pac4j will handle this

          case Failure(exception) =>
            println(s"❌ SecurityModule: Authentication error for email: $email - ${exception.getMessage}")
            exception.printStackTrace()
          // For void method, we don't set profile on failure
        }
      } catch {
        case e: Exception =>
          println(s"❌ SecurityModule: Unexpected error in authenticator: ${e.getMessage}")
          e.printStackTrace()
        // For void method, exceptions don't return anything
      }
    }
  }

  // Create form client with database authenticator
  val formClient = {
    val client = new FormClient("/login", new DatabaseAuthenticator())

    // Configure the form parameter names (default is usually fine)
    client.setUsernameParameter("email")
    client.setPasswordParameter("password")

    println(s"🔐 SecurityModule: Created FormClient with DatabaseAuthenticator")
    client
  }

  // Configure pac4j with proper callback URL
  val clients = new Clients("http://localhost:9000/callback", formClient)
  println(s"🔐 SecurityModule: Created Clients with callback URL: http://localhost:9000/callback")

  val config = new Config(clients)
  println(s"🔐 SecurityModule: Created Pac4j Config")

  // Set the default clients (Pac4j 4.5.7 API)
  config.setClients(clients)
  println(s"🔐 SecurityModule: Set clients in config")
  
  // Debug: Print configuration details
  println(s"🔐 SecurityModule: Config clients: ${config.getClients}")
  println(s"🔐 SecurityModule: FormClient name: ${formClient.getName}")
  println(s"🔐 SecurityModule: FormClient login URL: ${formClient.getLoginUrl}")

  // Get Pac4j config for filters
  def getPac4jConfig: Config = config
}