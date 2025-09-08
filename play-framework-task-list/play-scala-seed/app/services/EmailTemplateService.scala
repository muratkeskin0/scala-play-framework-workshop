package services

import javax.inject._
import play.api.Environment
import models.{EmailMessage, EmailType}
import scala.io.Source
import scala.util.{Try, Success, Failure}
import scala.concurrent.{ExecutionContext, Future}

trait IEmailTemplateService {
  def generateWelcomeEmail(to: String, username: String): EmailMessage
  def generateTaskReminderEmail(to: String, username: String, taskCount: Int): EmailMessage
  def generateCustomEmail(to: String, subject: String, body: String): EmailMessage
  def generateHtmlBody(emailMessage: EmailMessage): Future[String]
  def generateHtmlBodySync(emailMessage: EmailMessage): String
  def generateTextBody(emailMessage: EmailMessage): Future[String]
  def generateTextBodySync(emailMessage: EmailMessage): String
}

@Singleton
class EmailTemplateService @Inject()(env: Environment)(implicit ec: ExecutionContext) extends IEmailTemplateService {

  private val templatePath = "mailtemplates"

  // Email content generation - Template-based
  override def generateWelcomeEmail(to: String, username: String): EmailMessage = {
    EmailMessage(
      to = to,
      subject = "Welcome to Task Manager!",
      body = "", // Will be generated from template
      emailType = EmailType.WelcomeEmail
    )
  }

  override def generateTaskReminderEmail(to: String, username: String, taskCount: Int): EmailMessage = {
    EmailMessage(
      to = to,
      subject = s"You have $taskCount pending tasks",
      body = "", // Will be generated from template
      emailType = EmailType.TaskReminder
    )
  }

  override def generateCustomEmail(to: String, subject: String, body: String): EmailMessage = {
    EmailMessage(
      to = to,
      subject = subject,
      body = body,
      emailType = EmailType.WelcomeEmail // Default type
    )
  }

  // HTML generation
  override def generateHtmlBody(emailMessage: EmailMessage): Future[String] = {
    Future {
      generateHtmlBodySync(emailMessage)
    }
  }

  override def generateHtmlBodySync(emailMessage: EmailMessage): String = {
    val templateName = getTemplateName(emailMessage.emailType)
    val template = loadTemplate(templateName)
    replaceVariables(template, emailMessage)
  }

  // Text generation
  override def generateTextBody(emailMessage: EmailMessage): Future[String] = {
    Future {
      generateTextBodySync(emailMessage)
    }
  }

  override def generateTextBodySync(emailMessage: EmailMessage): String = {
    val templateName = getTemplateName(emailMessage.emailType)
    val template = loadTemplate(templateName)
    val htmlContent = replaceVariables(template, emailMessage)
    htmlToText(htmlContent)
  }

  // Private helper methods
  private def getTemplateName(emailType: EmailType): String = {
    emailType match {
      case EmailType.WelcomeEmail => "welcome"
      case EmailType.TaskReminder => "task_reminder"
      case _ => "default"
    }
  }

  private def loadTemplate(templateName: String): String = {
    loadTemplateWithFallback(templateName, isFallback = false)
  }
  
  private def loadTemplateWithFallback(templateName: String, isFallback: Boolean): String = {
    val templateFile = s"$templatePath/$templateName.html"
    
    Try {
      env.resourceAsStream(templateFile) match {
        case Some(resource) =>
          try {
            val content = Source.fromInputStream(resource, "UTF-8").mkString
            content
          } finally {
            resource.close()
          }
        case None =>
          throw new RuntimeException(s"Template file not found: $templateFile")
      }
    } match {
      case Success(content) => content
      case Failure(exception) => 
        // Fallback to default template if specific template not found and not already trying default
        if (templateName != "default" && !isFallback) {
          loadTemplateWithFallback("default", isFallback = true)
        } else {
          throw new RuntimeException(s"Failed to load template: $templateName", exception)
        }
    }
  }

  private def replaceVariables(template: String, emailMessage: EmailMessage): String = {
    emailMessage.emailType match {
      case EmailType.WelcomeEmail =>
        val username = emailMessage.to.split("@").head
        template.replace("{{username}}", username)
        
      case EmailType.TaskReminder =>
        val username = emailMessage.to.split("@").head
        val taskCount = extractTaskCount(emailMessage.body)
        template
          .replace("{{username}}", username)
          .replace("{{taskCount}}", taskCount.toString)
          .replace("{{taskCountPlural}}", if (taskCount > 1) "s" else "")
          
      case _ =>
        template
          .replace("{{subject}}", emailMessage.subject)
          .replace("{{body}}", emailMessage.body.replace("\n", "<br>"))
    }
  }

  private def htmlToText(html: String): String = {
    html
      .replaceAll("<[^>]+>", "") // Remove HTML tags
      .replaceAll("&nbsp;", " ")
      .replaceAll("&amp;", "&")
      .replaceAll("&lt;", "<")
      .replaceAll("&gt;", ">")
      .replaceAll("&quot;", "\"")
      .replaceAll("&#39;", "'")
      .replaceAll("\\s+", " ") // Replace multiple spaces with single space
      .trim
  }

  private def extractTaskCount(body: String): Int = {
    val pattern = "You have (\\d+) pending task".r
    pattern.findFirstMatchIn(body).map(_.group(1).toInt).getOrElse(0)
  }
}