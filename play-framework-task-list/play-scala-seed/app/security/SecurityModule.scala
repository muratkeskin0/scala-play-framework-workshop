package security

import javax.inject.Inject
import org.pac4j.core.config.Config
import org.pac4j.core.client.Clients
import org.pac4j.core.credentials.authenticator.Authenticator
import org.pac4j.core.credentials.UsernamePasswordCredentials
import org.pac4j.core.profile.CommonProfile
import org.pac4j.http.client.indirect.FormClient
import org.pac4j.jwt.config.encryption.SecretEncryptionConfiguration
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration
import org.pac4j.jwt.profile.JwtGenerator
import play.api.Configuration
import play.api.mvc.Request
import services.IUserService
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure}
import java.util.Optional

class SecurityModule @Inject()(userService: IUserService, configuration: Configuration)(implicit ec: ExecutionContext) {

  // JWT Configuration
  private val jwtSecret = configuration.get[String]("pac4j.jwt.secret")
  private val expirationTime = configuration.get[Int]("pac4j.jwt.expirationTime")
  private val issuer = configuration.get[String]("pac4j.jwt.issuer")
  private val audience = configuration.get[String]("pac4j.jwt.audience")
  
  // Pac4j JWT configurations
  private val signatureConfig = new SecretSignatureConfiguration(jwtSecret)
  private val jwtGenerator = new JwtGenerator(signatureConfig)

  // Custom authenticator for database authentication
  class DatabaseAuthenticator extends Authenticator {
    override def validate(callContext: org.pac4j.core.context.CallContext, credentials: org.pac4j.core.credentials.Credentials): Optional[org.pac4j.core.credentials.Credentials] = {
      credentials match {
        case upc: UsernamePasswordCredentials =>
          val email = upc.getUsername
          val password = upc.getPassword
          
          // Authenticate user against database
          userService.authenticate(email, password).onComplete {
            case Success(Some(user)) =>
              val profile = new CommonProfile()
              profile.setId(user.email)
              profile.addAttribute("email", user.email)
              profile.addAttribute("userId", user.id.toString)
              upc.setUserProfile(profile)
            case Success(None) =>
              // Invalid credentials
            case Failure(exception) =>
              // Authentication error
          }
          
          Optional.of(upc)
        case _ =>
          Optional.empty()
      }
    }
  }

  // Create form client with database authenticator
  val formClient = new FormClient("/login", new DatabaseAuthenticator())

  // Configure pac4j
  val config = new Config(new Clients(formClient))

  // Helper method to check if user is authenticated
  def isAuthenticated(request: Request[_]): Boolean = {
    request.session.get("email").isDefined
  }

  // Helper method to get user email from session
  def getCurrentUserEmail(request: Request[_]): Option[String] = {
    request.session.get("email")
  }

  // Pac4j JWT Token Generation
  def generateJwtToken(profile: CommonProfile): String = {
    try {
      println(s"🎫 SecurityModule: Generating JWT token for user: ${profile.getId}")
      
      // Set JWT claims
      profile.addAttribute("iss", issuer)
      profile.addAttribute("aud", audience)
      profile.addAttribute("exp", System.currentTimeMillis() / 1000 + expirationTime)
      
      // Generate JWT token using Pac4j
      val token = jwtGenerator.generate(profile)
      
      println(s"✅ SecurityModule: JWT token generated successfully")
      token
    } catch {
      case e: Exception =>
        println(s"❌ SecurityModule JWT Error: ${e.getMessage}")
        throw e
    }
  }
  
  // Create a Pac4j profile from user data
  def createProfile(email: String, userId: String, username: String): CommonProfile = {
    val profile = new CommonProfile()
    profile.setId(email)
    profile.addAttribute("email", email)
    profile.addAttribute("userId", userId)
    profile.addAttribute("username", username)
    profile
  }
  
  // Get JWT configuration info
  def getJWTInfo: Map[String, String] = Map(
    "issuer" -> issuer,
    "audience" -> audience,
    "expirationTime" -> expirationTime.toString,
    "secretLength" -> jwtSecret.length.toString
  )
}