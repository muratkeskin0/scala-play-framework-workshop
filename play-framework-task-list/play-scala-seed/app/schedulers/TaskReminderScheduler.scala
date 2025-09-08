package schedulers

import javax.inject._
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import services.{IUserService, ITaskService, IEmailHelperService}
import play.api.Logger

/**
 * Checks users' open task counts every hour and sends reminder emails.
 * Uses EmailHelperService which is a wrapper around EmailActorManager for fire-and-forget sending.
 */
@Singleton
class TaskReminderScheduler @Inject()(
  actorSystem: ActorSystem,
  userService: IUserService,
  taskService: ITaskService,
  emailHelper: IEmailHelperService
)(implicit ec: ExecutionContext) {

  private val initialDelay: FiniteDuration = 1.minute
  private val interval: FiniteDuration = 1.minute
  private val logger = Logger(this.getClass)

  actorSystem.scheduler.scheduleAtFixedRate(initialDelay, interval) { () =>
    runOnce()
  }

  private def runOnce(): Unit = {
    logger.info("TaskReminderScheduler run started")
    userService
      .list()
      .flatMap { users =>
        val emails = users.map(_.email)
        taskService.countByEmails(emails).map { counts => (users, counts) }
      }
      .map { case (users, counts) =>
        var sent = 0
        users.foreach { user =>
          val count = counts.getOrElse(user.email, 0)
          if (count > 0) {
            val username = user.email.split("@").headOption.getOrElse(user.email)
            emailHelper.sendTaskReminderEmail(user.email, username, count)
            sent += 1
          }
        }
        logger.info(s"TaskReminderScheduler run finished. Emails sent: $sent")
      }
      .recover { case ex =>
        logger.error(s"TaskReminderScheduler run failed: ${ex.getMessage}", ex)
      }
  }
}


