package configurations

import slick.jdbc.SQLServerProfile.api._
import models.{User, Role}

class Users(tag: Tag) extends Table[User](tag, "USERS") {
  def id       = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  def email    = column[String]("EMAIL", O.Unique)
  def password = column[String]("PASSWORD")
  def role     = column[String]("ROLE")

  def * = (id, email, password, role) <> (
    { case (id, email, password, roleStr) => User(id, email, password, Role.fromString(roleStr).getOrElse(Role.Basic)) },
    (user: User) => Some((user.id, user.email, user.password, Role.toString(user.role)))
  )
}

object Users {
  val users = TableQuery[Users]
}