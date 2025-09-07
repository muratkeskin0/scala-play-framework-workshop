package repositories

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.SQLServerProfile.api._
import models._
import configurations.Users
import persistence._

trait IUserRepository {
  def create(user: User): Future[Long]                // INSERT → yeni id döner
  def getByEmail(email: String): Future[Option[User]] // SELECT tek user by email
  def get(username: String): Future[Option[User]]     // SELECT tek user (backward compatibility)
  def list(): Future[Seq[User]]                       // SELECT *
  def update(user: User): Future[Int]                 // UPDATE → etkilenen satır sayısı
  def delete(email: String): Future[Int]              // DELETE → etkilenen satır sayısı
}

@Singleton
class UserRepository @Inject()(dbService: IDatabaseService)(implicit ec: ExecutionContext) extends IUserRepository {

  private val users = Users.users

  override def create(user: User): Future[Long] = {
    // id AutoInc olduğu için returning ile yeni id'yi alıyoruz
    val insert = (users returning users.map(_.id)) += user
    dbService.run(insert)
  }

  override def getByEmail(email: String): Future[Option[User]] = {
    dbService.run(users.filter(_.email === email).result.headOption)
  }

  override def get(username: String): Future[Option[User]] = {
    // Backward compatibility - treat username as email
    getByEmail(username)
  }

  override def list(): Future[Seq[User]] = {
    dbService.run(users.result)
  }

  override def update(user: User): Future[Int] = {
    val q = users.filter(_.id === user.id)
      .map(u => (u.email, u.password))
      .update((user.email, user.password))
    dbService.run(q)
  }

  override def delete(email: String): Future[Int] = {
    dbService.run(users.filter(_.email === email).delete)
  }
}
