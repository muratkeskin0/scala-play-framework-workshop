package services

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import repositories._
import models._

sealed trait UserError { def message: String }
object UserError {
  final case class AlreadyExists(username: String) extends UserError {
    val message = s"User '$username' already exists."
  }
  final case class NotFound(username: String) extends UserError {
    val message = s"User '$username' not found."
  }
  case object InvalidCredentials extends UserError {
    val message = "Invalid username or password."
  }
  case object InvalidInput extends UserError {
    val message = "Invalid input."
  }
}

trait IUserService {
  def signUp(username: String, password: String): Future[Either[UserError, User]]
  def login(username: String, password: String): Future[Either[UserError, User]]
  def get(username: String): Future[Option[User]]
  def list(): Future[Seq[User]]
}

@Singleton
class UserService @Inject()(repo: IUserRepository)(implicit ec: ExecutionContext) extends IUserService {
  import UserError._

  private def valid(username: String, password: String): Boolean =
    username.trim.nonEmpty && password.nonEmpty

  override def signUp(username: String, password: String): Future[Either[UserError, User]] = {
    val u = username.trim
    if (!valid(u, password)) Future.successful(Left(InvalidInput))
    else
      repo.get(u).flatMap {
        case Some(_) => Future.successful(Left(AlreadyExists(u)))
        case None =>
          val draft = User(id = 0L, username = u, password = password) // hash yok
          repo.create(draft).map { newId =>
            if (newId > 0) Right(draft.copy(id = newId))
            else Left(AlreadyExists(u)) // unique ihlali vs.
          }
      }
  }

  override def login(username: String, password: String): Future[Either[UserError, User]] = {
    val u = username.trim
    repo.get(u).map {
      case Some(user) if user.password == password => Right(user) // düz karşılaştırma
      case _                                       => Left(InvalidCredentials)
    }
  }

  override def get(username: String): Future[Option[User]] =
    repo.get(username.trim)

  override def list(): Future[Seq[User]] =
    repo.list()
}
