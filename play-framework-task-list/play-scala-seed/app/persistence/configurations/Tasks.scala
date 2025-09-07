package configurations

import slick.jdbc.SQLServerProfile.api._
import models.Task

class Tasks(tag: Tag) extends Table[Task](tag, "TASKS") {
  def id          = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  def userId      = column[Long]("USER_ID")
  def description = column[String]("DESCRIPTION")

  def * = (id, userId, description) <> (Task.tupled, Task.unapply)

  // Foreign key: TASKS.USER_ID → USERS.ID
  def user = foreignKey("FK_TASK_USER", userId, Users.users)(_.id, onDelete = ForeignKeyAction.Cascade)
}

object Tasks {
  val tasks = TableQuery[Tasks]
}
