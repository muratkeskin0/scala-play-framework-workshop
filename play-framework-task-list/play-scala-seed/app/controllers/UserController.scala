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
          Redirect(routes.AuthController.login())
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
            Redirect(routes.AuthController.login())
              .flashing("error" -> error.message)
        }
      }
    )
  }

  def profile() = Action.async { implicit request =>
    request.session.get("email") match {
      case None =>
        Future.successful(Redirect(routes.AuthController.login()).flashing("error" -> "Please login first"))
      case Some(email) =>
        userService.get(email).map {
          case Some(user) => Ok(views.html.profile(user))
          case None => Redirect(routes.AuthController.login()).flashing("error" -> "User not found")
        }
    }
  }

  def updateProfile() = Action.async { implicit request =>
    request.session.get("email") match {
      case None =>
        Future.successful(Redirect(routes.AuthController.login()).flashing("error" -> "Please login first"))
      case Some(currentEmail) =>
        updateProfileForm.bindFromRequest().fold(
          formWithErrors => {
            Future.successful(
              Redirect(routes.UserController.profile())
                .flashing("error" -> "Please enter a valid email address.")
            )
          },
          profileData => {
            userService.updateEmail(currentEmail, profileData.email).map {
              case Right(_) =>
                Redirect(routes.UserController.profile())
                  .withSession("email" -> profileData.email)
                  .flashing("success" -> "Email updated successfully!")
              case Left(error) =>
                val messageType = if (error == services.UserError.SameEmail) "info" else "error"
                Redirect(routes.UserController.profile())
                  .flashing(messageType -> error.message)
            }
          }
        )
    }
  }

  def changePassword() = Action.async { implicit request =>
    request.session.get("email") match {
      case None =>
        Future.successful(Redirect(routes.AuthController.login()).flashing("error" -> "Please login first"))
      case Some(email) =>
        changePasswordForm.bindFromRequest().fold(
          formWithErrors => {
            Future.successful(
              Redirect(routes.UserController.profile())
                .flashing("error" -> "Please check your input and try again.")
            )
          },
          passwordData => {
            userService.changePassword(email, passwordData.currentPassword, passwordData.newPassword).map {
              case Right(_) =>
                Redirect(routes.UserController.profile())
                  .flashing("success" -> "Password changed successfully!")
              case Left(error) =>
                Redirect(routes.UserController.profile())
                  .flashing("error" -> error.message)
            }
          }
        )
    }
  }

}
