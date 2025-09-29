package repositories

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.SQLServerProfile.api._
import models.{LawInfo, Country}
import configurations.LawInfoTable
import persistence._

trait ILawInfoRepository {
  def getByCountry(country: Country): Future[Option[LawInfo]]
  def getAllActive(): Future[Seq[LawInfo]]
  def create(info: LawInfo): Future[Long]
  def update(info: LawInfo): Future[Int]
  def delete(id: Long): Future[Int]
  def list(): Future[Seq[LawInfo]]
}

@Singleton
class LawInfoRepository @Inject()(dbService: IDatabaseService)(implicit ec: ExecutionContext) extends ILawInfoRepository {

  private val lawInfo = LawInfoTable.lawInfo

  override def getByCountry(country: Country): Future[Option[LawInfo]] = {
    val query = lawInfo
      .filter(_.country === Country.toString(country))
      .filter(_.isActive === true)
      .result
      .headOption
    
    dbService.run(query)
  }

  override def getAllActive(): Future[Seq[LawInfo]] = {
    val query = lawInfo
      .filter(_.isActive === true)
      .sortBy(_.createdAt.desc)
      .result
    
    dbService.run(query)
  }

  override def create(info: LawInfo): Future[Long] = {
    val insert = (lawInfo returning lawInfo.map(_.id)) += info
    dbService.run(insert)
  }

  override def update(info: LawInfo): Future[Int] = {
    val query = lawInfo
      .filter(_.id === info.id)
      .map(s => (s.country, s.title, s.content, s.isActive))
      .update((Country.toString(info.country), info.title, info.content, info.isActive))
    
    dbService.run(query)
  }

  override def delete(id: Long): Future[Int] = {
    dbService.run(lawInfo.filter(_.id === id).delete)
  }

  override def list(): Future[Seq[LawInfo]] = {
    val query = lawInfo
      .sortBy(_.createdAt.desc)
      .result
    
    dbService.run(query)
  }
}
