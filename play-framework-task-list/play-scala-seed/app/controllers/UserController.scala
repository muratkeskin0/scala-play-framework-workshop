package controllers

import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.i18n.I18nSupport
import services._
import services.UserError._
import forms.UserForms._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserController @Inject()(val controllerComponents: ControllerComponents, userService: IUserService)(implicit ec: ExecutionContext) extends BaseController with I18nSupport {

  def signUpValidate() = Action.async { implicit request =>
    signUpForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(
          Redirect(routes.HomeController.signUp())
            .flashing("error" -> "Please check your input and try again.")
        )
      },
      signUpData => {
        userService.signUp(signUpData.email, signUpData.password).map {
          case Right(_) =>
            Redirect(routes.TaskController.taskList())
              .withSession("email" -> signUpData.email)
              .flashing("success" -> "Account created successfully!")
          case Left(error) =>
            Redirect(routes.HomeController.signUp())
              .flashing("error" -> error.message)
        }
      }
    )
  }

  def loginValidate() = Action.async { implicit request =>
    loginForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(
          Redirect(routes.HomeController.login())
            .flashing("error" -> "Please check your input and try again.")
        )
      },
      loginData => {
        userService.login(loginData.email, loginData.password).map {
          case Right(_) =>
            Redirect(routes.TaskController.taskList())
              .withSession("email" -> loginData.email)
              .flashing("success" -> "Login successful!")
          case Left(error) =>
            Redirect(routes.HomeController.login())
              .flashing("error" -> error.message)
        }
      }
    )
  }

}
