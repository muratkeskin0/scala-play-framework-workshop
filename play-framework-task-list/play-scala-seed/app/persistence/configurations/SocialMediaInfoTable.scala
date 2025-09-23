package configurations

import slick.jdbc.SQLServerProfile.api._
import models.{SocialMediaInfo, Country}
import java.time.LocalDateTime

class SocialMediaInfoTable(tag: Tag) extends Table[SocialMediaInfo](tag, "SOCIAL_MEDIA_INFO") {
  def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  def country = column[String]("COUNTRY")
  def title = column[String]("TITLE")
  def content = column[String]("CONTENT")
  def isActive = column[Boolean]("IS_ACTIVE")
  def createdAt = column[LocalDateTime]("CREATED_AT")

  def * = (id, country, title, content, isActive, createdAt) <> (
    { case (id, countryStr, title, content, isActive, createdAt) => 
        SocialMediaInfo(id, Country.fromString(countryStr).getOrElse(Country.Other), title, content, isActive, createdAt) },
    (info: SocialMediaInfo) => Some((info.id, Country.toString(info.country), info.title, info.content, info.isActive, info.createdAt))
  )
}

object SocialMediaInfoTable {
  val socialMediaInfo = TableQuery[SocialMediaInfoTable]
}
