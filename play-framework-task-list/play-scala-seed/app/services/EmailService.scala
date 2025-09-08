package services

import javax.inject._
import play.api.libs.mailer._
import models.{EmailMessage, EmailResult}
import scala.concurrent.{ExecutionContext, Future}

trait IEmailService {
  def sendEmail(emailMessage: EmailMessage): Future[EmailResult]
}

@Singleton
class EmailService @Inject()(
  mailerClient: MailerClient,
  templateService: IEmailTemplateService
)(implicit ec: ExecutionContext) extends IEmailService {

  override def sendEmail(emailMessage: EmailMessage): Future[EmailResult] = {
    for {
      htmlBody <- templateService.generateHtmlBody(emailMessage)
      textBody <- templateService.generateTextBody(emailMessage)
    } yield {
      val email = Email(
        subject = emailMessage.subject,
        from = "noreply@taskmanager.com",
        to = Seq(emailMessage.to),
        bodyText = Some(textBody),
        bodyHtml = Some(htmlBody)
      )

      try {
        val messageId = mailerClient.send(email)
        EmailResult(messageId, success = true)
      } catch {
        case ex: Exception =>
          EmailResult("", success = false, Some(ex.getMessage))
      }
    }
  }
}
