package controllers

import javax.inject._
import play.api.mvc._
import services._
import services.UserError._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserController @Inject()(val controllerComponents: ControllerComponents, userService: IUserService)(implicit ec: ExecutionContext) extends BaseController {

  def signUpValidate() = Action.async { implicit request =>
    request.body.asFormUrlEncoded match {
      case Some(args) =>
        val username = args.get("username").flatMap(_.headOption).getOrElse("")
        val password = args.get("password").flatMap(_.headOption).getOrElse("")

        userService.signUp(username, password).map {
          case Right(_) =>
            Redirect(routes.TaskController.taskList())
              .withSession("username" -> username)
              .flashing("success" -> "Account created successfully!")
          case Left(error) =>
            Redirect(routes.HomeController.signUp())
              .flashing("error" -> error.message)
        }

      case None =>
        Future.successful(Ok("An Error Has Occured"))
    }
  }

  def loginValidate() = Action.async { implicit request =>
    request.body.asFormUrlEncoded match {
      case Some(args) =>
        val username = args.get("username").flatMap(_.headOption).getOrElse("")
        val password = args.get("password").flatMap(_.headOption).getOrElse("")

        userService.login(username, password).map {
          case Right(_) =>
            Redirect(routes.TaskController.taskList())
              .withSession("username" -> username)
              .flashing("success" -> "Login successful!")
          case Left(error) =>
            Redirect(routes.HomeController.login())
              .flashing("error" -> error.message)
        }

      case None =>
        Future.successful(Ok("An Error Has Occured"))
    }
  }
}
