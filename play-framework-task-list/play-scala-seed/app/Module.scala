import com.google.inject.AbstractModule
import repositories._
import services._
import persistence._
import actors._

class Module extends AbstractModule {
  override def configure(): Unit = {
    // Database service binding - en önce bu olmalı
    bind(classOf[IDatabaseService]).to(classOf[DatabaseService]).asEagerSingleton()
    
    // Repository bindings
    bind(classOf[ITaskRepository]).to(classOf[TaskRepository]).asEagerSingleton()
    bind(classOf[IUserRepository]).to(classOf[UserRepository]).asEagerSingleton()
    
    // Service bindings
    bind(classOf[ITaskService]).to(classOf[TaskService]).asEagerSingleton()
    bind(classOf[IUserService]).to(classOf[UserService]).asEagerSingleton()
    bind(classOf[IEmailService]).to(classOf[EmailService]).asEagerSingleton()
    bind(classOf[IEmailTemplateService]).to(classOf[EmailTemplateService]).asEagerSingleton()
    bind(classOf[IEmailHelperService]).to(classOf[EmailHelperService]).asEagerSingleton()
    
    // Actor bindings
    bind(classOf[EmailActorManager]).asEagerSingleton()
  }
}
