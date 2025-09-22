package controllers

import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json._
import services.{ITaskService, IUserService}
import forms.TaskForms._
import models.{User, Role, Task}
import security.SessionFactory
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.ProfileManager
import org.pac4j.play.PlayWebContext
import org.pac4j.play.store.PlaySessionStore
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

@Singleton
class TaskController @Inject()(val controllerComponents: ControllerComponents, taskService: ITaskService, userService: IUserService, sessionFactory: SessionFactory, sessionStore: PlaySessionStore)(implicit ec: ExecutionContext) extends BaseController with I18nSupport {

  // Helper method to get current user from Pac4j profile or session
  private def getCurrentUser(request: Request[_]): Future[Option[User]] = {
    try {
      // Try to get user from Pac4j profile using SessionFactory
      val webContext = sessionFactory.createWebContext(request)
      val profiles = sessionFactory.getProfiles(webContext)
      
      if (!profiles.isEmpty) {
        val profile = profiles.get(0).asInstanceOf[CommonProfile]
        val email = profile.getId
        println(s"📋 TaskController: Getting user from Pac4j profile: $email")
        userService.get(email)
      } else {
        // Fallback to session data
        val sessionEmail = request.session.get("email")
        if (sessionEmail.isDefined) {
          println(s"📋 TaskController: Getting user from session data: ${sessionEmail.get}")
          userService.get(sessionEmail.get)
        } else {
          println(s"❌ TaskController: No Pac4j profile or session data found")
          Future.successful(None)
        }
      }
    } catch {
      case e: Exception =>
        println(s"❌ TaskController: Error getting user: ${e.getMessage}")
        Future.successful(None)
    }
  }

  def taskList() = Action.async { implicit request =>
    getCurrentUser(request).flatMap {
      case Some(user) =>
        taskService.list(user.email).map { tasks =>
          Ok(views.html.taskList(tasks, Some(user)))
        }
      case None =>
        Future.successful(Redirect(routes.AuthController.login()).flashing("error" -> "User not found"))
    }
  }

  def modernTaskList() = Action.async { implicit request =>
    getCurrentUser(request).flatMap {
      case Some(user) =>
        taskService.list(user.email).map { tasks =>
          Ok(views.html.modernTaskList(tasks, Some(user)))
        }
      case None =>
        Future.successful(Redirect(routes.AuthController.login()).flashing("error" -> "User not found"))
    }
  }

  def addTask() = Action.async { implicit request =>
    getCurrentUser(request).flatMap {
      case Some(user) =>
        addTaskForm.bindFromRequest().fold(
          formWithErrors => {
            Future.successful(
              Redirect(routes.TaskController.taskList())
                .flashing("error" -> "Please enter a valid task description.")
            )
          },
          taskData => {
            taskService.add(user.email, taskData.description).map {
              case Right(_)  => Redirect(routes.TaskController.taskList()).flashing("success" -> "Task added!")
              case Left(err) => Redirect(routes.TaskController.taskList()).flashing("error"   -> err.message)
            }.recover { case _ =>
              Redirect(routes.TaskController.taskList()).flashing("error" -> "Unexpected error while adding task")
            }
          }
        )
      case None =>
        Future.successful(Redirect(routes.AuthController.login()).flashing("error" -> "User not found"))
    }
  }

  def updateTask() = Action.async { implicit request =>
    getCurrentUser(request).flatMap {
      case Some(user) =>
        updateTaskForm.bindFromRequest().fold(
          formWithErrors => {
            Future.successful(
              Redirect(routes.TaskController.taskList())
                .flashing("error" -> "Please enter a valid task description.")
            )
          },
          updateData => {
            taskService.update(user.email, updateData.id, updateData.description).map {
              case Right(_)  => Redirect(routes.TaskController.taskList()).flashing("success" -> "Task updated!")
              case Left(err) => Redirect(routes.TaskController.taskList()).flashing("error"   -> err.message)
            }.recover { case _ =>
              Redirect(routes.TaskController.taskList()).flashing("error" -> "Unexpected error while updating task")
            }
          }
        )
      case None =>
        Future.successful(Redirect(routes.AuthController.login()).flashing("error" -> "User not found"))
    }
  }

  def deleteTask() = Action.async { implicit request =>
    getCurrentUser(request).flatMap {
      case Some(user) =>
        deleteTaskForm.bindFromRequest().fold(
          formWithErrors => {
            Future.successful(
              Redirect(routes.TaskController.taskList())
                .flashing("error" -> "Invalid task ID.")
            )
          },
          deleteData => {
            taskService.delete(user.email, deleteData.id).map {
              case Right(_)  => Redirect(routes.TaskController.taskList()).flashing("success" -> "Task deleted!")
              case Left(err) => Redirect(routes.TaskController.taskList()).flashing("error"   -> err.message)
            }.recover { case _ =>
              Redirect(routes.TaskController.taskList()).flashing("error" -> "Unexpected error while deleting task")
            }
          }
        )
      case None =>
        Future.successful(Redirect(routes.AuthController.login()).flashing("error" -> "User not found"))
    }
  }

  // ========================================
  // AJAX ENDPOINTS FOR MODERN UI
  // ========================================

  // Get all tasks as JSON for AJAX
  def getTasksAjax() = Action.async { implicit request =>
    getCurrentUser(request).flatMap {
      case Some(user) =>
        taskService.list(user.email).map { tasks =>
          val taskJson = tasks.map(task => Json.obj(
            "id" -> task.id,
            "description" -> task.description
          ))
          Ok(Json.obj(
            "success" -> true,
            "tasks" -> taskJson
          ))
        }
      case None =>
        Future.successful(Unauthorized(Json.obj(
          "success" -> false,
          "message" -> "User not authenticated"
        )))
    }
  }

  // Add task via AJAX
  def addTaskAjax() = Action.async(parse.json) { implicit request =>
    getCurrentUser(request).flatMap {
      case Some(user) =>
        val description = (request.body \ "description").as[String]
        
        if (description.trim.isEmpty) {
          Future.successful(BadRequest(Json.obj(
            "success" -> false,
            "message" -> "Task description cannot be empty"
          )))
        } else {
          taskService.add(user.email, description.trim).map {
            case Right(newTask) =>
              Ok(Json.obj(
                "success" -> true,
                "message" -> "Task added successfully!",
                "task" -> Json.obj(
                  "id" -> newTask.id,
                  "description" -> newTask.description
                )
              ))
            case Left(err) =>
              BadRequest(Json.obj(
                "success" -> false,
                "message" -> err.message
              ))
          }.recover { case _ =>
            InternalServerError(Json.obj(
              "success" -> false,
              "message" -> "Unexpected error while adding task"
            ))
          }
        }
      case None =>
        Future.successful(Unauthorized(Json.obj(
          "success" -> false,
          "message" -> "User not authenticated"
        )))
    }
  }

  // Update task via AJAX
  def updateTaskAjax(taskId: Long) = Action.async(parse.json) { implicit request =>
    getCurrentUser(request).flatMap {
      case Some(user) =>
        val description = (request.body \ "description").as[String]
        
        if (description.trim.isEmpty) {
          Future.successful(BadRequest(Json.obj(
            "success" -> false,
            "message" -> "Task description cannot be empty"
          )))
        } else {
          taskService.update(user.email, taskId, description.trim).map {
            case Right(updatedTask) =>
              Ok(Json.obj(
                "success" -> true,
                "message" -> "Task updated successfully!",
                "task" -> Json.obj(
                  "id" -> updatedTask.id,
                  "description" -> updatedTask.description
                )
              ))
            case Left(err) =>
              BadRequest(Json.obj(
                "success" -> false,
                "message" -> err.message
              ))
          }.recover { case _ =>
            InternalServerError(Json.obj(
              "success" -> false,
              "message" -> "Unexpected error while updating task"
            ))
          }
        }
      case None =>
        Future.successful(Unauthorized(Json.obj(
          "success" -> false,
          "message" -> "User not authenticated"
        )))
    }
  }

  // Delete task via AJAX
  def deleteTaskAjax(taskId: Long) = Action.async { implicit request =>
    getCurrentUser(request).flatMap {
      case Some(user) =>
        taskService.delete(user.email, taskId).map {
          case Right(_) =>
            Ok(Json.obj(
              "success" -> true,
              "message" -> "Task deleted successfully!"
            ))
          case Left(err) =>
            BadRequest(Json.obj(
              "success" -> false,
              "message" -> err.message
            ))
        }.recover { case _ =>
          InternalServerError(Json.obj(
            "success" -> false,
            "message" -> "Unexpected error while deleting task"
          ))
        }
      case None =>
        Future.successful(Unauthorized(Json.obj(
          "success" -> false,
          "message" -> "User not authenticated"
        )))
    }
  }

}
