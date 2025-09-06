package repositories

import javax.inject._
import scala.collection.concurrent.TrieMap
import scala.collection.mutable.ListBuffer
import models._

trait ITaskRepository {
  def getAll(username: String): Seq[Task]
  def add(task: Task): Boolean
  def delete(username: String, index: Int): Boolean
  def get(username: String, index: Int): Option[Task]
  def update(username: String, index: Int, task: Task): Boolean
}

@Singleton
class TaskRepository @Inject()() extends ITaskRepository {

  private val store: TrieMap[String, ListBuffer[Task]] = TrieMap.empty

  override def getAll(username: String): Seq[Task] =
    store.getOrElseUpdate(username, ListBuffer.empty).synchronized {
      store(username).toList
    }

  override def add(task: Task): Boolean = {
    val buf = store.getOrElseUpdate(task.username, ListBuffer.empty)
    buf.synchronized {
      buf += task
      true
    }
  }

  override def delete(username: String, index: Int): Boolean = {
    val buf = store.getOrElse(username, ListBuffer.empty)
    buf.synchronized {
      if (index >= 0 && index < buf.length) {
        buf.remove(index)
        true
      } else false
    }
  }

  override def get(username: String, index: Int): Option[Task] = {
    val tasks = store.getOrElse(username, ListBuffer.empty)
    tasks.synchronized {
      if (index >= 0 && index < tasks.length) {
        Some(tasks(index))
      } else None
    }
  }

  override def update(username: String, index: Int, task: Task): Boolean = {
    val buf = store.getOrElse(username, ListBuffer.empty)
    buf.synchronized {
      if (index >= 0 && index < buf.length) {
        buf.update(index, task)
        true
      } else false
    }
  }
}
