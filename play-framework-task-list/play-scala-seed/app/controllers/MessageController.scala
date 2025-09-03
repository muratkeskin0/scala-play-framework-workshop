package controllers

import javax.inject._
import play.api.mvc._
import play.api.i18n._

@Singleton
class MessageController @Inject()(cc: MessagesControllerComponents)
  extends MessagesAbstractController(cc) {

  def indexWithMessage() = Action { implicit request: MessagesRequest[AnyContent] =>
    val msg = Messages("greeting")
    Ok(views.html.indexWithMessage(msg))
  }

}
