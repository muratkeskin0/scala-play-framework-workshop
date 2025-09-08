package forms

import play.api.data.Form
import play.api.data.Forms._

object TaskForms {

  case class AddTaskData(description: String)
  case class UpdateTaskData(id: Long, description: String)
  case class DeleteTaskData(id: Long)

  val addTaskForm: Form[AddTaskData] = Form(
    mapping(
      "description" -> nonEmptyText(minLength = 1, maxLength = 500)
        .verifying("Task description cannot be empty", _.trim.nonEmpty)
    )(AddTaskData.apply)(AddTaskData.unapply)
  )

  val updateTaskForm: Form[UpdateTaskData] = Form(
    mapping(
      "id" -> longNumber(min = 1),
      "description" -> nonEmptyText(minLength = 1, maxLength = 500)
        .verifying("Task description cannot be empty", _.trim.nonEmpty)
    )(UpdateTaskData.apply)(UpdateTaskData.unapply)
  )

  val deleteTaskForm: Form[DeleteTaskData] = Form(
    mapping(
      "id" -> longNumber(min = 1)
    )(DeleteTaskData.apply)(DeleteTaskData.unapply)
  )
}
