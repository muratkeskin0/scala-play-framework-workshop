package configurations

import slick.jdbc.SQLServerProfile.api._
import models.{LawInfo, Country}
import java.time.LocalDateTime

class LawInfoTable(tag: Tag) extends Table[LawInfo](tag, "LAW_INFO") {
  def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  def country = column[String]("COUNTRY")
  def title = column[String]("TITLE")
  def content = column[String]("CONTENT")
  def isActive = column[Boolean]("IS_ACTIVE")
  def createdAt = column[LocalDateTime]("CREATED_AT")

  def * = (id, country, title, content, isActive, createdAt) <> (
    { case (id, countryStr, title, content, isActive, createdAt) => 
        LawInfo(id, Country.fromString(countryStr).getOrElse(Country.Other), title, content, isActive, createdAt) },
    (info: LawInfo) => Some((info.id, Country.toString(info.country), info.title, info.content, info.isActive, info.createdAt))
  )
}

object LawInfoTable {
  val lawInfo = TableQuery[LawInfoTable]
}
