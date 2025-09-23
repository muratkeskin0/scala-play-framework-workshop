package controllers

import javax.inject._
import play.api.mvc._
import play.api.i18n.I18nSupport
import services.{ISocialMediaInfoService, IUserService}
import models.{User, Country}
import security.SessionFactory
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SocialMediaInfoController @Inject()(
  val controllerComponents: ControllerComponents,
  socialMediaInfoService: ISocialMediaInfoService,
  userService: IUserService,
  sessionFactory: SessionFactory
)(implicit ec: ExecutionContext) extends BaseController with I18nSupport {

  // Helper method to get current user
  private def getCurrentUser(request: Request[_]): Future[Option[User]] = {
    try {
      val webContext = sessionFactory.createWebContext(request)
      val profiles = sessionFactory.getProfiles(webContext)
      
      if (!profiles.isEmpty) {
        val profile = profiles.get(0)
        val email = profile.getId
        userService.get(email)
      } else {
        request.session.get("email") match {
          case Some(email) => userService.get(email)
          case None => Future.successful(None)
        }
      }
    } catch {
      case e: Exception =>
        println(s"❌ SocialMediaInfoController: Error getting user: ${e.getMessage}")
        Future.successful(None)
    }
  }

  def socialMediaGuidelines() = Action.async { implicit request =>
    getCurrentUser(request).flatMap {
      case Some(user) =>
        // Get all social media guidelines
        socialMediaInfoService.getAllActive().map { allInfo =>
          Ok(views.html.allSocialMediaGuidelines(allInfo, Some(user)))
        }
      case None =>
        Future.successful(Redirect(routes.AuthController.login()).flashing("error" -> "Please login first"))
    }
  }
}
