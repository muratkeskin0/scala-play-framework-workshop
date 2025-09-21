package security

import javax.inject._
import org.pac4j.core.context.WebContext
import org.pac4j.core.profile.ProfileManager
import org.pac4j.core.profile.CommonProfile
import org.pac4j.play.PlayWebContext
import org.pac4j.play.store.PlaySessionStore
import play.api.mvc.RequestHeader

@Singleton
class SessionFactory @Inject()(sessionStore: PlaySessionStore) {
  
  /**
   * Factory method to create a consistent PlayWebContext
   * This ensures both AuthController and Pac4jSecurityFilter use the same session context
   */
  def createWebContext(request: RequestHeader): PlayWebContext = {
    val webContext = new PlayWebContext(request, sessionStore)
    println(s"🏭 SessionFactory: Created WebContext for session: ${request.session.data.getOrElse("email", "unknown")}")
    webContext
  }
  
  /**
   * Factory method to create a ProfileManager with consistent configuration
   */
  def createProfileManager(webContext: PlayWebContext): ProfileManager[CommonProfile] = {
    new ProfileManager[CommonProfile](webContext, sessionStore)
  }
  
  /**
   * Factory method to save a profile with consistent settings
   */
  def saveProfile(webContext: PlayWebContext, profile: CommonProfile): Boolean = {
    try {
      val profileManager = createProfileManager(webContext)
      profileManager.save(true, profile, false)
      println(s"🏭 SessionFactory: Successfully saved profile for user: ${profile.getId}")
      true
    } catch {
      case e: Exception =>
        println(s"❌ SessionFactory: Failed to save profile: ${e.getMessage}")
        false
    }
  }
  
  /**
   * Factory method to retrieve profiles with consistent settings
   */
  def getProfiles(webContext: PlayWebContext): java.util.List[CommonProfile] = {
    try {
      val profileManager = createProfileManager(webContext)
      val profiles = profileManager.getAll(true)
      println(s"🏭 SessionFactory: Retrieved ${profiles.size} profiles")
      profiles
    } catch {
      case e: Exception =>
        println(s"❌ SessionFactory: Failed to retrieve profiles: ${e.getMessage}")
        new java.util.ArrayList[CommonProfile]()
    }
  }
  
  /**
   * Get the shared session store instance
   */
  def getSessionStore: PlaySessionStore = sessionStore
}
