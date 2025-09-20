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
import org.pac4j.jwt.profile.JwtProfile
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator
import play.api.Configuration
import play.api.mvc.Request
import services.IUserService
import models.{User, Role}
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
  private val jwtAuthenticator = new JwtAuthenticator(signatureConfig)

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
  
  // JWT Token Validation
  def validateJwtToken(token: String): Future[Option[JwtProfile]] = {
    Future {
      try {
        println(s"🔍 SecurityModule: Validating JWT token...")
        
        // Create JWT credentials
        val credentials = new org.pac4j.core.credentials.TokenCredentials(token)
        
        // Validate token using Pac4j JWT authenticator
        val callContext = new org.pac4j.core.context.CallContext(null, null)
        val validatedCredentials = jwtAuthenticator.validate(callContext, credentials)
        
        if (validatedCredentials.isPresent) {
          val profile = validatedCredentials.get().getUserProfile.asInstanceOf[JwtProfile]
          println(s"✅ SecurityModule: JWT token validated successfully for user: ${profile.getId}")
          Some(profile)
        } else {
          println(s"❌ SecurityModule: JWT token validation failed")
          None
        }
      } catch {
        case e: Exception =>
          println(s"❌ SecurityModule JWT Validation Error: ${e.getMessage}")
          None
      }
    }
  }
  
  // Extract JWT token from Authorization header
  def extractTokenFromHeader(authHeader: String): Option[String] = {
    if (authHeader.startsWith("Bearer ")) {
      Some(authHeader.substring(7))
    } else {
      None
    }
  }
  
  // Validate JWT token and return user info
  def validateAndGetUser(token: String): Future[Option[Map[String, String]]] = {
    validateJwtToken(token).map {
      case Some(profile) =>
        val userId = profile.getAttribute("userId", classOf[String])
        val username = profile.getAttribute("username", classOf[String])
        
        Some(Map(
          "email" -> profile.getId,
          "userId" -> (if (userId != null) userId else ""),
          "username" -> (if (username != null) username else "")
        ))
      case None => None
    }
  }

  // JWT Authentication Helper for Controllers
  def authenticateRequest(request: Request[_]): Future[Option[CommonProfile]] = {
    request.headers.get("Authorization") match {
      case Some(authHeader) =>
        extractTokenFromHeader(authHeader) match {
          case Some(token) =>
            validateJwtToken(token).map {
              case Some(profile) => Some(profile)
              case None => None
            }
          case None => Future.successful(None)
        }
      case None => Future.successful(None)
    }
  }
  
  // Get user from JWT token
  def getCurrentUser(request: Request[_]): Future[Option[User]] = {
    authenticateRequest(request).flatMap {
      case Some(profile) =>
        val email = profile.getId
        userService.get(email)
      case None => Future.successful(None)
    }
  }
  
  // Check if user is admin from JWT
  def isAdmin(request: Request[_]): Future[Boolean] = {
    getCurrentUser(request).map {
      case Some(user) => user.role == Role.Admin
      case None => false
    }
  }

  // Get JWT configuration info
  def getJWTInfo: Map[String, String] = Map(
    "issuer" -> issuer,
    "audience" -> audience,
    "expirationTime" -> expirationTime.toString,
    "secretLength" -> jwtSecret.length.toString
  )
}