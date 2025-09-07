package repositories

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.SQLServerProfile.api._
import models._
import configurations.Users  // senin Users tablon

trait IUserRepository {
  def create(user: User): Future[Long]                // INSERT → yeni id döner
  def get(username: String): Future[Option[User]]     // SELECT tek user
  def list(): Future[Seq[User]]                       // SELECT *
  def update(user: User): Future[Int]                 // UPDATE → etkilenen satır sayısı
  def delete(username: String): Future[Int]           // DELETE → etkilenen satır sayısı
}

@Singleton
class UserRepository @Inject() (implicit ec: ExecutionContext) extends IUserRepository {

  private val db = Database.forConfig("slick.dbs.default.db") // application.conf içindeki ayar
  private val users = Users.users

  override def create(user: User): Future[Long] = {
    // id AutoInc olduğu için returning ile yeni id’yi alıyoruz
    val insert = (users returning users.map(_.id)) += user
    db.run(insert)
  }

  override def get(username: String): Future[Option[User]] = {
    db.run(users.filter(_.username === username).result.headOption)
  }

  override def list(): Future[Seq[User]] = {
    db.run(users.result)
  }

  override def update(user: User): Future[Int] = {
    val q = users.filter(_.id === user.id)
      .map(u => (u.username, u.password))
      .update((user.username, user.password))
    db.run(q)
  }

  override def delete(username: String): Future[Int] = {
    db.run(users.filter(_.username === username).delete)
  }
}
