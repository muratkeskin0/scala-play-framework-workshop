package services

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import repositories.ILawInfoRepository
import models.{LawInfo, Country}

trait ILawInfoService {
  def getByCountry(country: Country): Future[Option[LawInfo]]
  def getAllActive(): Future[Seq[LawInfo]]
  def create(info: LawInfo): Future[Long]
  def update(info: LawInfo): Future[Boolean]
  def delete(id: Long): Future[Boolean]
  def list(): Future[Seq[LawInfo]]
}

@Singleton
class LawInfoService @Inject()(repo: ILawInfoRepository)(implicit ec: ExecutionContext) extends ILawInfoService {

  override def getByCountry(country: Country): Future[Option[LawInfo]] = {
    repo.getByCountry(country)
  }

  override def getAllActive(): Future[Seq[LawInfo]] = {
    repo.getAllActive()
  }

  override def create(info: LawInfo): Future[Long] = {
    repo.create(info)
  }

  override def update(info: LawInfo): Future[Boolean] = {
    repo.update(info).map(_ > 0)
  }

  override def delete(id: Long): Future[Boolean] = {
    repo.delete(id).map(_ > 0)
  }

  override def list(): Future[Seq[LawInfo]] = {
    repo.list()
  }
}
