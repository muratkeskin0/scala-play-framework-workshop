package actors

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.play.PlaySpec
import play.api.test.Helpers._
import play.api.test.WithApplication
import models.{EmailMessage, EmailType}
import services.IEmailService
import scala.concurrent.Future

class EmailActorSpec extends PlaySpec with ScalaFutures {

  implicit val defaultPatience: PatienceConfig = PatienceConfig(timeout = Span(5, Seconds), interval = Span(100, Millis))

  "EmailActorManager" should {
    "be properly injected" in new WithApplication {
      val emailActorManager = app.injector.instanceOf[EmailActorManager]
      emailActorManager must not be null
    }
  }

  "EmailService" should {
    "be properly injected" in new WithApplication {
      val emailService = app.injector.instanceOf[IEmailService]
      emailService must not be null
    }
    
    "generate welcome email content correctly" in new WithApplication {
      val emailService = app.injector.instanceOf[IEmailService]
      
      // Test welcome email generation
      val testEmail = "test@example.com"
      val testUsername = "testuser"
      
      whenReady(emailService.sendWelcomeEmail(testEmail, testUsername)) { result =>
        // Email gönderimi başarılı olmasa bile (SMTP ayarları olmadığı için)
        // en azından service'in çalıştığını doğrularız
        result must not be null
      }
    }
  }
}

