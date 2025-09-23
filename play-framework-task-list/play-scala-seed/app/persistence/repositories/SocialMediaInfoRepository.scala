package repositories

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.SQLServerProfile.api._
import models.{SocialMediaInfo, Country}
import configurations.SocialMediaInfoTable
import persistence._

trait ISocialMediaInfoRepository {
  def getByCountry(country: Country): Future[Option[SocialMediaInfo]]
  def getAllActive(): Future[Seq[SocialMediaInfo]]
  def create(info: SocialMediaInfo): Future[Long]
  def update(info: SocialMediaInfo): Future[Int]
  def delete(id: Long): Future[Int]
  def list(): Future[Seq[SocialMediaInfo]]
}

@Singleton
class SocialMediaInfoRepository @Inject()(dbService: IDatabaseService)(implicit ec: ExecutionContext) extends ISocialMediaInfoRepository {

  private val socialMediaInfo = SocialMediaInfoTable.socialMediaInfo

  override def getByCountry(country: Country): Future[Option[SocialMediaInfo]] = {
    val query = socialMediaInfo
      .filter(_.country === Country.toString(country))
      .filter(_.isActive === true)
      .result
      .headOption
    
    dbService.run(query)
  }

  override def getAllActive(): Future[Seq[SocialMediaInfo]] = {
    val query = socialMediaInfo
      .filter(_.isActive === true)
      .sortBy(_.createdAt.desc)
      .result
    
    dbService.run(query)
  }

  override def create(info: SocialMediaInfo): Future[Long] = {
    val insert = (socialMediaInfo returning socialMediaInfo.map(_.id)) += info
    dbService.run(insert)
  }

  override def update(info: SocialMediaInfo): Future[Int] = {
    val query = socialMediaInfo
      .filter(_.id === info.id)
      .map(s => (s.country, s.title, s.content, s.isActive))
      .update((Country.toString(info.country), info.title, info.content, info.isActive))
    
    dbService.run(query)
  }

  override def delete(id: Long): Future[Int] = {
    dbService.run(socialMediaInfo.filter(_.id === id).delete)
  }

  override def list(): Future[Seq[SocialMediaInfo]] = {
    val query = socialMediaInfo
      .sortBy(_.createdAt.desc)
      .result
    
    dbService.run(query)
  }
}
