package controllers

import javax.inject._
import play.api.mvc._
import play.api.i18n._
import play.api.data._
import play.api.data.Forms._
import models.TaskListInMemoryModel


case class SignUpData(username:String, password:String)

@Singleton
class MessageController @Inject()(cc: MessagesControllerComponents)
  extends MessagesAbstractController(cc) {

  def indexWithMessage() = Action { implicit request: MessagesRequest[AnyContent] =>
    val msg = Messages("greeting")
    Ok(views.html.indexWithMessage(msg))
  }

  private val signUpForm: Form[SignUpData] = Form(
    mapping(
      "username" -> nonEmptyText(minLength = 3, maxLength = 32),
      "password" -> nonEmptyText(minLength = 6)
    )(SignUpData.apply)(SignUpData.unapply)
  )

  def signUpWithForm: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.signUpWithForm(signUpForm))  // render the form with helpers
  }

  def signUpWithFormValidate = Action { implicit request =>
    signUpForm.bindFromRequest.fold(
      formWithErrors => {
        // If form validation fails, show the form again with errors
        BadRequest(views.html.signUpWithForm(formWithErrors))
      },
      data => {
        // data is SignUpData(username, password)
        if (TaskListInMemoryModel.createUser(data.username, data.password)) {
          Redirect(routes.TaskListController.taskList())
            .withSession("username" -> data.username)
            .flashing("success" -> s"Account created for ${data.username}")
        } else {
          Redirect(routes.MessageController.signUpWithForm())
            .flashing("error" -> "Username already exists")
        }
      }
    )
  }

}
