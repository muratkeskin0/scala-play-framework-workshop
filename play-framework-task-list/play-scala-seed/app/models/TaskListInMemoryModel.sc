package models

import scala.collection.mutable


object TaskListInMemoryModel {
  private val users = mutable.Map[String,String]("Murat" -> "Pass123")

  def validateUser(username:String, password: String): Boolean = {
    users.get(username).map(_ == password).getOrElse(false)
  }
  def validateUserWithIfExist(username:String, password: String): Boolean = {
    users.get(username).exists(_ == password)
  }
  def validateUserWithContain(username:String, password: String): Boolean = {
    users.get(username).contains(password)
  }

  def createUser(username:String, password: String): Boolean = ???
  def addTask(username:String, task:String): Boolean = ???
  def deleteTask(username:String, index:Int): Boolean = ???
  def getAllTasks(username:String): Seq[String] = ???
}