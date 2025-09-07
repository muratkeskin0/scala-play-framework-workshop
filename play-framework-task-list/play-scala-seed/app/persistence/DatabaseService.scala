package persistence

import javax.inject._
import slick.jdbc.SQLServerProfile.api._
import scala.concurrent.{ExecutionContext, Future}
import play.api.Configuration

trait IDatabaseService {
  def database: Database
  def run[T](action: DBIO[T]): Future[T]
  def close(): Future[Unit]
}

@Singleton
class DatabaseService @Inject()(config: Configuration)(implicit ec: ExecutionContext) extends IDatabaseService {
  
  // Database connection'ı tek yerden yönetiyoruz
  private val db: Database = Database.forConfig("slick.dbs.default.db", config.underlying)
  
  override def database: Database = db
  
  override def run[T](action: DBIO[T]): Future[T] = {
    db.run(action)
  }
  
  override def close(): Future[Unit] = {
    Future {
      db.close()
    }
  }
}
