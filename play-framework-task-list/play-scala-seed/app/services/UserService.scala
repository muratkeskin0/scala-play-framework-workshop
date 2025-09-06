package services

import javax.inject._
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
  def signUp(username: String, password: String): Either[UserError, User]
  def login(username: String, password: String): Either[UserError, User]
  def get(username: String): Option[User]
  def list(): Seq[User]
}

@Singleton
class UserService @Inject()(repo: IUserRepository) extends IUserService {
  import UserError._

  private def valid(username: String, password: String): Boolean =
    username.trim.nonEmpty && password.length >= 1 // şimdilik basit

  override def signUp(username: String, password: String): Either[UserError, User] = {
    if (!valid(username, password)) return Left(InvalidInput)
    repo.get(username.trim) match {
      case Some(_) => Left(AlreadyExists(username))
      case None =>
        val user = User(username.trim, password)
        if (repo.create(user)) Right(user) else Left(AlreadyExists(username))
    }
  }

  override def login(username: String, password: String): Either[UserError, User] =
    repo.get(username.trim) match {
      case Some(u) if u.password == password => Right(u)
      case _ => Left(InvalidCredentials)
    }

  override def get(username: String): Option[User] = repo.get(username.trim)
  override def list(): Seq[User] = repo.list()
}
