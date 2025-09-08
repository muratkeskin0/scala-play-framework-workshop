package services

import javax.inject._
import models.{EmailMessage, EmailType}
import scala.concurrent.{ExecutionContext, Future}
import actors._

trait IEmailHelperService {
  def sendWelcomeEmail(to: String, username: String): Unit
  def sendTaskReminderEmail(to: String, username: String, taskCount: Int): Unit
}

@Singleton
class EmailHelperService @Inject()(
  templateService: IEmailTemplateService,
  emailActorManager: EmailActorManager
)(implicit ec: ExecutionContext) extends IEmailHelperService {

  override def sendWelcomeEmail(to: String, username: String): Unit = {
    try {
      val welcomeEmail = templateService.generateWelcomeEmail(to, username)
      emailActorManager.sendEmail(welcomeEmail)
    } catch {
      case ex: Exception =>
        println(s"WARNING: Failed to send welcome email to $to: ${ex.getMessage}")
    }
  }

  override def sendTaskReminderEmail(to: String, username: String, taskCount: Int): Unit = {
    try {
      val reminderEmail = templateService.generateTaskReminderEmail(to, username, taskCount)
      emailActorManager.sendEmail(reminderEmail)
    } catch {
      case ex: Exception =>
        println(s"WARNING: Failed to send task reminder email to $to: ${ex.getMessage}")
    }
  }
}

