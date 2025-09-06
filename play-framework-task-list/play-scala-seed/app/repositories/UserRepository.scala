package repositories

import javax.inject._
import scala.collection.concurrent.TrieMap
import models._

trait IUserRepository {
  def create(user: User): Boolean                 // C: kullanıcı yoksa ekler
  def get(username: String): Option[User]         // R: tek kullanıcıyı getir
  def list(): Seq[User]                           // R: tüm kullanıcılar
  def update(user: User): Boolean                 // U: varsa günceller
  def delete(username: String): Boolean           // D: siler
}

@Singleton
class UserRepository @Inject()() extends IUserRepository {

  // username -> User
  private val users = TrieMap.empty[String, User]

  override def create(user: User): Boolean =
    users.putIfAbsent(user.username, user).isEmpty

  override def get(username: String): Option[User] =
    users.get(username)

  override def list(): Seq[User] =
    users.values.toSeq

  override def update(user: User): Boolean =
    users.replace(user.username, user).isDefined

  override def delete(username: String): Boolean =
    users.remove(username).isDefined

}
