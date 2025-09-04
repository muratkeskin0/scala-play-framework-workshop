package models

import scala.collection.mutable


object TaskListInMemoryModel {
  private val users = mutable.Map[String,String]("Murat" -> "Pass123")
  private val tasks = mutable.Map[String,Seq[String]]("Murat" -> List("do homework", "code", "eat"))

  def validateUser(username:String, password: String): Boolean = {
    users.get(username).map(_ == password).getOrElse(false)
  }
  def validateUserWithIfExist(username:String, password: String): Boolean = {
    users.get(username).exists(_ == password)
  }
  def validateUserWithContain(username:String, password: String): Boolean = {
    users.get(username).contains(password)
  }

  def createUser(username:String, password: String): Boolean = {
    if(users.contains(username)) return false
    else{
      users(username) = password
      return true
    }
  }

  def addTask(username:String, task:String): Boolean = {
    if(!users.contains(username)) return false
    else{
      val existing = tasks.getOrElse(username, Nil)
      tasks(username) = existing :+ task       // append new task
      true
    }
  }

  def deleteTask(username: String, index: Int): Boolean = {
    if (!users.contains(username)) return false

    tasks.get(username) match {
      case Some(ts) if index >= 0 && index < ts.length =>
        tasks(username) = ts.patch(index, Nil, 1) // remove element at index
        true
      case _ =>
        false
    }
  }

  def getAllTasks(username:String): Seq[String] = {
    tasks.get(username).getOrElse(Nil)
  }
}