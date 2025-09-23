package services

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import models.{SocialMediaInfo, SocialMediaFilter, Country}
import repositories.ISocialMediaInfoRepository

trait ISocialMediaFilterService {
  def getFilteredGuidelines(filter: Option[SocialMediaFilter]): Future[Seq[SocialMediaInfo]]
}

@Singleton
class SocialMediaFilterService @Inject()(
  socialMediaInfoRepo: ISocialMediaInfoRepository
)(implicit ec: ExecutionContext) extends ISocialMediaFilterService {

  override def getFilteredGuidelines(filter: Option[SocialMediaFilter]): Future[Seq[SocialMediaInfo]] = {
    socialMediaInfoRepo.getAllActive().map { allInfos =>
      filter match {
        case Some(f) => f.apply(allInfos)
        case None => allInfos
      }
    }
  }

}
