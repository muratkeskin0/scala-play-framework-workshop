import com.google.inject.AbstractModule
import repositories._
import services._

class Module extends AbstractModule {
  override def configure(): Unit = {
    // Buraya binding kurallarını yazıyoruz
    bind(classOf[ITaskRepository]).to(classOf[TaskRepository]).asEagerSingleton()
    bind(classOf[IUserRepository]).to(classOf[UserRepository]).asEagerSingleton()
    bind(classOf[ITaskService]).to(classOf[TaskService]).asEagerSingleton()
    bind(classOf[IUserService]).to(classOf[UserService]).asEagerSingleton()
  }
}
