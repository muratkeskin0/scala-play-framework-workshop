package security

import javax.inject._
import org.pac4j.play.PlayWebContext
import org.pac4j.play.store.PlaySessionStore
import play.api.mvc.RequestHeader

@Singleton
class SessionFactory @Inject()(securityModule: SecurityModule) {
  
  // Use the same session store instance from SecurityModule
  private val sessionStore = securityModule.sessionStore

  /**
   * Factory method to create a consistent PlayWebContext
   * This ensures both AuthController and Pac4jSecurityFilter use the same session context
   */
  def createWebContext(request: RequestHeader): PlayWebContext = {
    new PlayWebContext(request, sessionStore)
  }

}