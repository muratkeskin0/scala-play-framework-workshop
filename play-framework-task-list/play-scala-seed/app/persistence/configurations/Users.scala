package configurations

import slick.jdbc.SQLServerProfile.api._
import models.{User, Role, Country}

class Users(tag: Tag) extends Table[User](tag, "USERS") {
  def id       = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  def email    = column[String]("EMAIL", O.Unique)
  def password = column[String]("PASSWORD")
  def role     = column[String]("ROLE")
  def country  = column[String]("COUNTRY")

  def * = (id, email, password, role, country) <> (
    { case (id, email, password, roleStr, countryStr) => 
        User(id, email, password, Role.fromString(roleStr).getOrElse(Role.Basic), Country.fromString(countryStr).getOrElse(Country.Other)) },
    (user: User) => Some((user.id, user.email, user.password, Role.toString(user.role), Country.toString(user.country)))
  )
}

object Users {
  val users = TableQuery[Users]
}