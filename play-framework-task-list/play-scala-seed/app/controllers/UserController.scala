package controllers

import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.i18n.I18nSupport
import services._
import services.UserError._
import forms.UserForms._
import models.{User, Role, Country}
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
        Country.fromString(signUpData.country) match {
          case Some(country) =>
            userService.signUp(signUpData.email, signUpData.password, country).map {
              case Right(_) =>
                Redirect(routes.TaskController.taskList())
                  .withSession("email" -> signUpData.email)
                  .flashing("success" -> "Account created successfully!")
              case Left(error) =>
                Redirect(routes.HomeController.signUp())
                  .flashing("error" -> error.message)
            }
          case None =>
            Future.successful(
              Redirect(routes.HomeController.signUp())
                .flashing("error" -> "Invalid country selected.")
            )
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
            Country.fromString(profileData.country) match {
              case Some(country) =>
                userService.updateProfile(currentEmail, profileData.email, country).map {
                  case Right(_) =>
                    Redirect(routes.UserController.profile())
                      .withSession("email" -> profileData.email)
                      .flashing("success" -> "Profile updated successfully!")
                  case Left(error) =>
                    val messageType = if (error == services.UserError.SameEmail) "info" else "error"
                    Redirect(routes.UserController.profile())
                      .flashing(messageType -> error.message)
                }
              case None =>
                Future.successful(
                  Redirect(routes.UserController.profile())
                    .flashing("error" -> "Invalid country selected.")
                )
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

  // Admin user management methods
  def updateUser() = Action.async { implicit request =>
    // Pac4jSecurityFilter ensures admin access
    val userId = request.body.asFormUrlEncoded.flatMap(_.get("id")).flatMap(_.headOption).map(_.toLong)
    val email = request.body.asFormUrlEncoded.flatMap(_.get("email")).flatMap(_.headOption)
    val roleStr = request.body.asFormUrlEncoded.flatMap(_.get("role")).flatMap(_.headOption)
    val countryStr = request.body.asFormUrlEncoded.flatMap(_.get("country")).flatMap(_.headOption)
    val password = request.body.asFormUrlEncoded.flatMap(_.get("password")).flatMap(_.headOption).filter(_.nonEmpty)

    (userId, email, roleStr, countryStr) match {
      case (Some(id), Some(newEmail), Some(role), Some(country)) =>
        val newRole = Role.fromString(role).getOrElse(Role.Basic)
        Country.fromString(country) match {
          case Some(newCountry) =>
            userService.getByEmail(newEmail).flatMap {
              case Some(existingUser) if existingUser.id != id =>
                // Email already exists for another user
                Future.successful(Redirect(routes.HomeController.adminDashboard()).flashing("error" -> "Email already exists"))
              case _ =>
                // Get the user to update
                userService.list().map(_.find(_.id == id)).flatMap {
                  case Some(user) =>
                    val updatedUser = user.copy(
                      email = newEmail,
                      role = newRole,
                      country = newCountry,
                      password = password.getOrElse(user.password)
                    )
                    userService.updateUser(updatedUser).map { result =>
                      if (result) {
                        Redirect(routes.HomeController.adminDashboard()).flashing("success" -> "User updated successfully!")
                      } else {
                        Redirect(routes.HomeController.adminDashboard()).flashing("error" -> "Failed to update user")
                      }
                    }
                  case None =>
                    Future.successful(Redirect(routes.HomeController.adminDashboard()).flashing("error" -> "User not found"))
                }
            }
          case None =>
            Future.successful(Redirect(routes.HomeController.adminDashboard()).flashing("error" -> "Invalid country selected"))
        }
      case _ =>
        Future.successful(Redirect(routes.HomeController.adminDashboard()).flashing("error" -> "Invalid form data"))
    }
  }

  def deleteUser() = Action.async { implicit request =>
    // Pac4jSecurityFilter ensures admin access
    val userId = request.body.asFormUrlEncoded.flatMap(_.get("id")).flatMap(_.headOption).map(_.toLong)
    userId match {
      case Some(id) =>
        userService.deleteUser(id).map { result =>
          if (result) {
            Redirect(routes.HomeController.adminDashboard()).flashing("success" -> "User deleted successfully!")
          } else {
            Redirect(routes.HomeController.adminDashboard()).flashing("error" -> "Failed to delete user")
          }
        }
      case None =>
        Future.successful(Redirect(routes.HomeController.adminDashboard()).flashing("error" -> "Invalid user ID"))
    }
  }

}
