package actors

import akka.actor.{ActorRef, ActorSystem, OneForOneStrategy, Props, SupervisorStrategy}
import akka.pattern.{Backoff, BackoffSupervisor}
import javax.inject._

import services.IEmailService
import models.EmailMessage

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

@Singleton
class EmailActorManager @Inject()(emailService: IEmailService, actorSystem: ActorSystem)(implicit ec: ExecutionContext) {

  private val childProps: Props = EmailActor.props(emailService)

  private val supervisorProps: Props =
    BackoffSupervisor.props(
      Backoff
        .onFailure(
          childProps,
          childName    = "email-actor",
          minBackoff   = 1.second,
          maxBackoff   = 30.seconds,
          randomFactor = 0.2
        )
        .withSupervisorStrategy(
          OneForOneStrategy(loggingEnabled = false) {
            case _: java.net.ConnectException      => SupervisorStrategy.Restart
            case _: javax.mail.SendFailedException => SupervisorStrategy.Resume
            case _: IllegalArgumentException       => SupervisorStrategy.Stop
            case _                                 => SupervisorStrategy.Restart
          }
        )
    )

  private val supervisorRef: ActorRef =
    actorSystem.actorOf(supervisorProps, "email-actor-supervisor")

  def sendEmail(emailMessage: EmailMessage): Unit =
    supervisorRef ! EmailActor.SendEmail(emailMessage)
}
