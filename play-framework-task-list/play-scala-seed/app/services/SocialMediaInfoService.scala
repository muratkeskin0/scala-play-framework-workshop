package services

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import repositories.ISocialMediaInfoRepository
import models.{SocialMediaInfo, Country}

trait ISocialMediaInfoService {
  def getByCountry(country: Country): Future[Option[SocialMediaInfo]]
  def getAllActive(): Future[Seq[SocialMediaInfo]]
  def create(info: SocialMediaInfo): Future[Long]
  def update(info: SocialMediaInfo): Future[Boolean]
  def delete(id: Long): Future[Boolean]
  def list(): Future[Seq[SocialMediaInfo]]
}

@Singleton
class SocialMediaInfoService @Inject()(repo: ISocialMediaInfoRepository)(implicit ec: ExecutionContext) extends ISocialMediaInfoService {

  override def getByCountry(country: Country): Future[Option[SocialMediaInfo]] = {
    repo.getByCountry(country)
  }

  override def getAllActive(): Future[Seq[SocialMediaInfo]] = {
    repo.getAllActive()
  }

  override def create(info: SocialMediaInfo): Future[Long] = {
    repo.create(info)
  }

  override def update(info: SocialMediaInfo): Future[Boolean] = {
    repo.update(info).map(_ > 0)
  }

  override def delete(id: Long): Future[Boolean] = {
    repo.delete(id).map(_ > 0)
  }

  override def list(): Future[Seq[SocialMediaInfo]] = {
    repo.list()
  }
}
