package models

import java.time.LocalDateTime

sealed trait EmailType
object EmailType {
  case object WelcomeEmail extends EmailType
  case object TaskReminder extends EmailType
}

final case class EmailMessage(
  to: String,
  subject: String,
  body: String,
  emailType: EmailType,
  sentAt: Option[LocalDateTime] = None
)

final case class EmailResult(
  messageId: String,
  success: Boolean,
  error: Option[String] = None
)

