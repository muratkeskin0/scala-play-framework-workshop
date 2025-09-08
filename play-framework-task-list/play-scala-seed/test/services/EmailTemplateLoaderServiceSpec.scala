package services

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.play.PlaySpec
import play.api.test.Helpers._
import play.api.test.WithApplication
import models.{EmailMessage, EmailType}

class EmailTemplateServiceSpec extends PlaySpec with ScalaFutures {

  implicit val defaultPatience: PatienceConfig = PatienceConfig(timeout = Span(5, Seconds), interval = Span(100, Millis))

  "EmailTemplateService" should {
    "generate welcome email successfully" in new WithApplication {
      val templateService = app.injector.instanceOf[IEmailTemplateService]
      
      val welcomeEmail = templateService.generateWelcomeEmail("test@example.com", "testuser")
      
      welcomeEmail.to mustBe "test@example.com"
      welcomeEmail.subject mustBe "Welcome to Task Manager!"
      welcomeEmail.emailType mustBe EmailType.WelcomeEmail
      welcomeEmail.body mustBe "" // Body will be generated from template
    }
    
    "generate task reminder email successfully" in new WithApplication {
      val templateService = app.injector.instanceOf[IEmailTemplateService]
      
      val reminderEmail = templateService.generateTaskReminderEmail("test@example.com", "testuser", 5)
      
      reminderEmail.to mustBe "test@example.com"
      reminderEmail.subject mustBe "You have 5 pending tasks"
      reminderEmail.emailType mustBe EmailType.TaskReminder
      reminderEmail.body mustBe "" // Body will be generated from template
    }
    
    "generate HTML body for welcome email" in new WithApplication {
      val templateService = app.injector.instanceOf[IEmailTemplateService]
      
      val welcomeEmail = templateService.generateWelcomeEmail("test@example.com", "testuser")
      
      whenReady(templateService.generateHtmlBody(welcomeEmail)) { html =>
        html must include("Welcome to Task Manager!")
        html must include("testuser")
        html must include("<!DOCTYPE html>")
        html must not include("{{username}}")
      }
    }
    
    "generate HTML body synchronously" in new WithApplication {
      val templateService = app.injector.instanceOf[IEmailTemplateService]
      
      val welcomeEmail = templateService.generateWelcomeEmail("test@example.com", "testuser")
      val html = templateService.generateHtmlBodySync(welcomeEmail)
      
      html must include("Welcome to Task Manager!")
      html must include("testuser")
      html must include("<!DOCTYPE html>")
      html must not include("{{username}}")
    }
    
    "generate text body for welcome email" in new WithApplication {
      val templateService = app.injector.instanceOf[IEmailTemplateService]
      
      val welcomeEmail = templateService.generateWelcomeEmail("test@example.com", "testuser")
      
      whenReady(templateService.generateTextBody(welcomeEmail)) { text =>
        text must include("Welcome to Task Manager!")
        text must include("testuser")
        text must not include("<")
        text must not include(">")
        text must not include("{{username}}")
      }
    }
    
    "generate text body synchronously" in new WithApplication {
      val templateService = app.injector.instanceOf[IEmailTemplateService]
      
      val welcomeEmail = templateService.generateWelcomeEmail("test@example.com", "testuser")
      val text = templateService.generateTextBodySync(welcomeEmail)
      
      text must include("Welcome to Task Manager!")
      text must include("testuser")
      text must not include("<")
      text must not include(">")
      text must not include("{{username}}")
    }
  }
}
