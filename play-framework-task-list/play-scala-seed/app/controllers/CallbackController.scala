package controllers

import javax.inject.Inject
import play.api.mvc._
import play.api.Configuration
import org.pac4j.core.config.Config
import org.pac4j.play.store.PlaySessionStore
import scala.concurrent.{ExecutionContext, Future}

class CallbackController @Inject()(
  val controllerComponents: ControllerComponents,
  config: Config,
  sessionStore: PlaySessionStore
)(implicit ec: ExecutionContext) extends BaseController {


  def callback() = Action.async { request =>
    Future {
      try {
        println(s"🔄 CallbackController: Processing callback for path: ${request.path}")
        
        // Since we're handling authentication directly in AuthController,
        // this callback just redirects to the task list
        println(s"✅ CallbackController: Callback processed successfully")
        Redirect(routes.TaskController.taskList())
          .flashing("success" -> "Authentication successful!")
      } catch {
        case e: Exception =>
          println(s"❌ CallbackController: Error processing callback - ${e.getMessage}")
          e.printStackTrace()
          Redirect(routes.AuthController.login())
            .flashing("error" -> "Authentication callback failed")
      }
    }
  }
}
