package actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import models.{EmailMessage, EmailResult}
import services.IEmailService
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

object EmailActor {
  case class SendEmail(emailMessage: EmailMessage)
  
  def props(emailService: IEmailService)(implicit ec: ExecutionContext): Props = Props(new EmailActor(emailService))
}

class EmailActor(emailService: IEmailService)(implicit ec: ExecutionContext) extends Actor with ActorLogging {
  import EmailActor._
  
  override def receive: Receive = {
    case SendEmail(emailMessage) =>
      log.info(s"Sending email to ${emailMessage.to} with subject: ${emailMessage.subject}")
      
      emailService.sendEmail(emailMessage).onComplete { result =>
        result match {
          case scala.util.Success(emailResult) =>
            log.info(s"Email sent successfully to ${emailMessage.to}. MessageId: ${emailResult.messageId}")
          case scala.util.Failure(exception) =>
            log.error(s"Failed to send email to ${emailMessage.to}: ${exception.getMessage}")
        }
      }
  }
}

@Singleton
class EmailActorManager @Inject()(emailService: IEmailService, actorSystem: ActorSystem)(implicit ec: ExecutionContext) {
  
  private val emailActor: ActorRef = actorSystem.actorOf(EmailActor.props(emailService), "email-actor")
  
  def sendEmail(emailMessage: EmailMessage): Unit = {
    emailActor ! EmailActor.SendEmail(emailMessage)
  }
}
