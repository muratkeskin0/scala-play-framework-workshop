package configurations

import slick.jdbc.SQLServerProfile.api._
import models.User

class Users(tag: Tag) extends Table[User](tag, "USERS") {
  def id       = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  def username = column[String]("USERNAME", O.Unique)
  def password = column[String]("PASSWORD")

  def * = (id, username, password) <> (User.tupled, User.unapply)
}

object Users {
  val users = TableQuery[Users]
}