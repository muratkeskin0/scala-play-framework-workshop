package security

import javax.inject._
import org.pac4j.core.context.WebContext
import org.pac4j.core.profile.ProfileManager
import org.pac4j.core.profile.CommonProfile
import org.pac4j.play.PlayWebContext
import org.pac4j.play.store.PlaySessionStore
import play.api.mvc.RequestHeader
import scala.jdk.CollectionConverters._

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
    new ProfileManager[CommonProfile](webContext)
  }

  /**
   * Factory method to save a profile with consistent settings
   */
  def saveProfile(webContext: PlayWebContext, profile: CommonProfile): Boolean = {
    try {
      val profileManager = createProfileManager(webContext)

      // For Pac4j 4.5.7, use the correct save method signature
      profileManager.save(true, profile, false)
      println(s"🏭 SessionFactory: Successfully saved profile for user: ${profile.getId}")

      // Verify the save worked by trying to retrieve it immediately
      val savedProfiles = profileManager.getAll(true)
      if (savedProfiles.size() > 0) {
        println(s"🏭 SessionFactory: Verification successful - profile persisted")
      } else {
        println(s"❌ SessionFactory: Verification failed - profile not persisted")
      }

      true
    } catch {
      case e: Exception =>
        println(s"❌ SessionFactory: Failed to save profile: ${e.getMessage}")
        e.printStackTrace()
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
      println(s"🏭 SessionFactory: Retrieved ${profiles.size} profiles from ProfileManager")

      // Convert UserProfile to CommonProfile
      val commonProfiles = new java.util.ArrayList[CommonProfile]()
      profiles.asScala.foreach { profile =>
        if (profile.isInstanceOf[CommonProfile]) {
          commonProfiles.add(profile.asInstanceOf[CommonProfile])
          println(s"🏭 SessionFactory: Found profile: ${profile.getId}")
        }
      }

      println(s"🏭 SessionFactory: Final profile count: ${commonProfiles.size}")
      commonProfiles
    } catch {
      case e: Exception =>
        println(s"❌ SessionFactory: Failed to retrieve profiles: ${e.getMessage}")
        e.printStackTrace()
        new java.util.ArrayList[CommonProfile]()
    }
  }

  /**
   * Get the shared session store instance
   */
  def getSessionStore: PlaySessionStore = sessionStore
}