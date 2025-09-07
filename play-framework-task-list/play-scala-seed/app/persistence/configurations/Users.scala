package configurations

import slick.jdbc.SQLServerProfile.api._
import models.User

class Users(tag: Tag) extends Table[User](tag, "USERS") {
  def id       = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  def email    = column[String]("EMAIL", O.Unique)
  def password = column[String]("PASSWORD")

  def * = (id, email, password) <> (User.tupled, User.unapply)
}

object Users {
  val users = TableQuery[Users]
}