package controllers

import javax.inject._
import play.api.mvc._
import services._
import services.UserError._

@Singleton
class UserController @Inject()(val controllerComponents: ControllerComponents, userService: IUserService) extends BaseController {

  def signUpValidate() = Action { implicit request =>
    request.body.asFormUrlEncoded.map { args =>
      val username = args.get("username").flatMap(_.headOption).getOrElse("")
      val password = args.get("password").flatMap(_.headOption).getOrElse("")

      userService.signUp(username, password) match {
        case Right(_) =>
          Redirect(routes.TaskController.taskList()).withSession("username" -> username).flashing("success" -> "Account created successfully!")
        case Left(error) =>
          Redirect(routes.HomeController.signUp()).flashing("error" -> error.message)
      }
    }.getOrElse(Ok("An Error Has Occured"))
  }

  def loginValidate() = Action { implicit request =>
    request.body.asFormUrlEncoded.map { args =>
      val username = args.get("username").flatMap(_.headOption).getOrElse("")
      val password = args.get("password").flatMap(_.headOption).getOrElse("")

      userService.login(username, password) match {
        case Right(_) =>
          Redirect(routes.TaskController.taskList()).withSession("username" -> username).flashing("success" -> "Login successful!")
        case Left(error) =>
          Redirect(routes.HomeController.login()).flashing("error" -> error.message)
      }
    }.getOrElse(Ok("An Error Has Occured"))
  }
}
