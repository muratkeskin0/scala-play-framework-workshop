package actors

import akka.actor.{Actor, ActorLogging, OneForOneStrategy, SupervisorStrategy}
import akka.pattern.pipe
import models.{EmailMessage, EmailResult}
import services.IEmailService

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

object EmailActor {
  final case class SendEmail(emailMessage: EmailMessage)

  private final case class EmailSent(result: EmailResult)
  private final case class EmailFailed(ex: Throwable)

  def props(emailService: IEmailService)(implicit ec: ExecutionContext) =
    akka.actor.Props(new EmailActor(emailService))
}

final class EmailActor(emailService: IEmailService)(implicit ec: ExecutionContext)
  extends Actor with ActorLogging {

  import EmailActor._
  import context.dispatcher

  override val supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
    case _: java.net.ConnectException        => SupervisorStrategy.Restart
    case _: javax.mail.SendFailedException   => SupervisorStrategy.Resume
    case _: IllegalArgumentException         => SupervisorStrategy.Stop
    case NonFatal(_)                         => SupervisorStrategy.Restart
  }

  override def receive: Receive = {
    case SendEmail(emailMessage) =>
      log.info(s"Sending email to ${emailMessage.to} with subject: ${emailMessage.subject}")

      emailService
        .sendEmail(emailMessage)
        .map(EmailSent.apply)
        .recover { case ex => EmailFailed(ex) }
        .pipeTo(self)

    case EmailSent(res) =>
      log.info(s"Email sent successfully. MessageId: ${res.messageId}")

    case EmailFailed(ex) =>
      log.error(s"Email send failed: ${ex.getMessage}")
      throw ex
  }
}


