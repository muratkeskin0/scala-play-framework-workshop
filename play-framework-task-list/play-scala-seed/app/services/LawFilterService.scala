package services

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import models.{LawInfo, LawFilter, Country}
import repositories.ILawInfoRepository

trait ILawFilterService {
  def getFilteredLaws(filter: Option[LawFilter]): Future[Seq[LawInfo]]
}

@Singleton
class LawFilterService @Inject()(
  lawInfoRepo: ILawInfoRepository
)(implicit ec: ExecutionContext) extends ILawFilterService {

  override def getFilteredLaws(filter: Option[LawFilter]): Future[Seq[LawInfo]] = {
    lawInfoRepo.getAllActive().map { allInfos =>
      filter match {
        case Some(f) => f.apply(allInfos)
        case None => allInfos
      }
    }
  }

}
