package models

final case class User(id: Long = 0L, email: String, password: String, role: Role = Role.Basic, country: Country = Country.Other)
