package controllers

import javax.inject._
import play.api.mvc._
import play.api.i18n.I18nSupport
import services.{ILawInfoService, IUserService, ILawFilterService}
import models.{User, Country, LawFilter}
import security.{SessionFactory, SecurityModule}
import org.pac4j.core.profile.CommonProfile
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LawInfoController @Inject()(
  val controllerComponents: ControllerComponents,
  lawInfoService: ILawInfoService,
  userService: IUserService,
  filterService: ILawFilterService,
  sessionFactory: SessionFactory,
  securityModule: SecurityModule
)(implicit ec: ExecutionContext) extends BaseController with I18nSupport {

  // Helper method to get current user
  private def getCurrentUser(request: Request[_]): Future[Option[User]] = {
    try {
      // WORKAROUND: Pac4j 4.5.7 ProfileManager bug - use direct session store access
      val webContext = sessionFactory.createWebContext(request)
      val sessionStore = securityModule.sessionStore
      
      // Try to get profile directly from session store
      val profileOption = sessionStore.get(webContext, "pac4j_profiles")
      
      if (profileOption.isPresent) {
        val profile = profileOption.get().asInstanceOf[CommonProfile]
        val email = profile.getId
        userService.get(email)
      } else {
        // Fallback to session data
        val sessionEmail = request.session.get("pac4j.userEmail")
        sessionEmail match {
          case Some(email) =>
            userService.get(email)
          case None =>
            Future.successful(None)
        }
      }
    } catch {
      case e: Exception =>
        println(s"❌ Error getting user: ${e.getMessage}")
        Future.successful(None)
    }
  }

  def countryLaws() = Action.async { implicit request =>
    getCurrentUser(request).flatMap {
      case Some(user) =>
        // Parse country filter from query parameters
        val countryFilter = LawFilter.fromQueryParams(request.queryString)
        
        // Get filtered laws
        filterService.getFilteredLaws(countryFilter).map { filteredInfos =>
          Ok(views.html.allCountryLaws(
            filteredInfos, 
            Some(user), 
            countryFilter
          ))
        }
      case None =>
        Future.successful(Redirect(routes.AuthController.login()).flashing("error" -> "Please login first"))
    }
  }
  
}
