package services

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import repositories._
import models._
import utils.EmailValidator
import actors.EmailActorManager

sealed trait UserError { def message: String }
object UserError {
  final case class AlreadyExists(email: String) extends UserError {
    val message = s"User with email '$email' already exists."
  }
  final case class NotFound(email: String) extends UserError {
    val message = s"User with email '$email' not found."
  }
  case object InvalidCredentials extends UserError {
    val message = "Invalid email or password."
  }
  case object InvalidInput extends UserError {
    val message = "Invalid input."
  }
  case object InvalidEmail extends UserError {
    val message = "Invalid email format."
  }
  case object EmailAlreadyExists extends UserError {
    val message = "Email already exists."
  }
  case object WrongCurrentPassword extends UserError {
    val message = "Current password is incorrect."
  }
  case object SameEmail extends UserError {
    val message = "Email is already the same."
  }
}

trait IUserService {
  def signUp(email: String, password: String): Future[Either[UserError, User]]
  def login(email: String, password: String): Future[Either[UserError, User]]
  def authenticate(email: String, password: String): Future[Option[User]]
  def get(email: String): Future[Option[User]]
  def getByEmail(email: String): Future[Option[User]]
  def list(): Future[Seq[User]]
  def updateEmail(currentEmail: String, newEmail: String): Future[Either[UserError, User]]
  def changePassword(email: String, currentPassword: String, newPassword: String): Future[Either[UserError, User]]
  def updateUser(user: User): Future[Boolean]
  def deleteUser(id: Long): Future[Boolean]
}

@Singleton
class UserService @Inject()(repo: IUserRepository, emailHelper: IEmailHelperService)(implicit ec: ExecutionContext) extends IUserService {
  import UserError._

  private def validPassword(password: String): Boolean =
    password.nonEmpty && password.length >= 6

  override def signUp(email: String, password: String): Future[Either[UserError, User]] = {
    EmailValidator.validateAndNormalize(email) match {
      case None => Future.successful(Left(InvalidEmail))
      case Some(normalizedEmail) =>
        if (!validPassword(password)) Future.successful(Left(InvalidInput))
        else
          repo.getByEmail(normalizedEmail).flatMap {
            case Some(_) => Future.successful(Left(AlreadyExists(normalizedEmail)))
            case None =>
              val draft = User(id = 0L, email = normalizedEmail, password = password)
              repo.create(draft).map { newId =>
                if (newId > 0) {
                  val createdUser = draft.copy(id = newId)
                  
                  // Asenkron olarak welcome email gönder (fire-and-forget)
                  emailHelper.sendWelcomeEmail(normalizedEmail, normalizedEmail.split("@").head)
                  
                  Right(createdUser)
                } else {
                  Left(AlreadyExists(normalizedEmail))
                }
              }
          }
    }
  }

  override def login(email: String, password: String): Future[Either[UserError, User]] = {
    EmailValidator.validateAndNormalize(email) match {
      case None => Future.successful(Left(InvalidEmail))
      case Some(normalizedEmail) =>
        repo.getByEmail(normalizedEmail).map {
          case Some(user) if user.password == password => Right(user)
          case _ => Left(InvalidCredentials)
        }
    }
  }

  override def authenticate(email: String, password: String): Future[Option[User]] = {
    EmailValidator.validateAndNormalize(email) match {
      case None => Future.successful(None)
      case Some(normalizedEmail) =>
        repo.getByEmail(normalizedEmail).map {
          case Some(user) if user.password == password => Some(user)
          case _ => None
        }
    }
  }

  override def get(email: String): Future[Option[User]] = {
    EmailValidator.validateAndNormalize(email) match {
      case None => Future.successful(None)
      case Some(normalizedEmail) => repo.getByEmail(normalizedEmail)
    }
  }

  override def getByEmail(email: String): Future[Option[User]] = {
    EmailValidator.validateAndNormalize(email) match {
      case None => Future.successful(None)
      case Some(normalizedEmail) => repo.getByEmail(normalizedEmail)
    }
  }

  override def list(): Future[Seq[User]] =
    repo.list()

  override def updateEmail(currentEmail: String, newEmail: String): Future[Either[UserError, User]] = {
    EmailValidator.validateAndNormalize(newEmail) match {
      case None => Future.successful(Left(InvalidEmail))
      case Some(normalizedNewEmail) =>
        if (normalizedNewEmail == currentEmail) {
          Future.successful(Left(SameEmail)) // Same email - show info message
        } else {
          // Check if new email already exists
          repo.getByEmail(normalizedNewEmail).flatMap {
            case Some(_) => Future.successful(Left(EmailAlreadyExists))
            case None =>
              // Get current user and update email
              repo.getByEmail(currentEmail).flatMap {
                case Some(user) =>
                  val updatedUser = user.copy(email = normalizedNewEmail)
                  repo.update(updatedUser).map { affected =>
                    if (affected > 0) Right(updatedUser) else Left(InvalidInput)
                  }
                case None => Future.successful(Left(NotFound(currentEmail)))
              }
          }
        }
    }
  }

  override def changePassword(email: String, currentPassword: String, newPassword: String): Future[Either[UserError, User]] = {
    if (!validPassword(newPassword)) {
      Future.successful(Left(InvalidInput))
    } else {
      repo.getByEmail(email).flatMap {
        case Some(user) if user.password == currentPassword =>
          val updatedUser = user.copy(password = newPassword)
          repo.update(updatedUser).map { affected =>
            if (affected > 0) Right(updatedUser) else Left(InvalidInput)
          }
        case Some(_) => Future.successful(Left(WrongCurrentPassword))
        case None => Future.successful(Left(NotFound(email)))
      }
    }
  }

  override def updateUser(user: User): Future[Boolean] = {
    repo.update(user).map(_ > 0)
  }

  override def deleteUser(id: Long): Future[Boolean] = {
    repo.list().flatMap { users =>
      users.find(_.id == id) match {
        case Some(user) =>
          repo.delete(user.email).map(_ > 0)
        case None =>
          Future.successful(false)
      }
    }
  }

}
