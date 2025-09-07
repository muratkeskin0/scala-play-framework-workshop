package repositories

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.SQLServerProfile.api._
import models._
import configurations.Tasks
import persistence._

trait ITaskRepository {
  def getAll(userId: Long): Future[Seq[Task]]
  def add(task: Task): Future[Long]
  def delete(taskId: Long): Future[Int]
  def get(taskId: Long): Future[Option[Task]]
  def update(task: Task): Future[Int]
}

@Singleton
class TaskRepository @Inject()(dbService: IDatabaseService)(implicit ec: ExecutionContext) extends ITaskRepository {

  private val tasks = Tasks.tasks

  override def getAll(userId: Long): Future[Seq[Task]] = {
    dbService.run(tasks.filter(_.userId === userId).result)
  }

  override def add(task: Task): Future[Long] = {
    val insert = (tasks returning tasks.map(_.id)) += task
    dbService.run(insert)
  }

  override def delete(taskId: Long): Future[Int] = {
    dbService.run(tasks.filter(_.id === taskId).delete)
  }

  override def get(taskId: Long): Future[Option[Task]] = {
    dbService.run(tasks.filter(_.id === taskId).result.headOption)
  }

  override def update(task: Task): Future[Int] = {
    val q = tasks.filter(_.id === task.id)
      .map(t => (t.userId, t.description))
      .update((task.userId, task.description))
    dbService.run(q)
  }
}
