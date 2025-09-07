package services

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import repositories._
import models._
import utils.EmailValidator

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
}

trait IUserService {
  def signUp(email: String, password: String): Future[Either[UserError, User]]
  def login(email: String, password: String): Future[Either[UserError, User]]
  def get(email: String): Future[Option[User]]
  def list(): Future[Seq[User]]
}

@Singleton
class UserService @Inject()(repo: IUserRepository)(implicit ec: ExecutionContext) extends IUserService {
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
                if (newId > 0) Right(draft.copy(id = newId))
                else Left(AlreadyExists(normalizedEmail))
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

  override def get(email: String): Future[Option[User]] = {
    EmailValidator.validateAndNormalize(email) match {
      case None => Future.successful(None)
      case Some(normalizedEmail) => repo.getByEmail(normalizedEmail)
    }
  }

  override def list(): Future[Seq[User]] =
    repo.list()
}
