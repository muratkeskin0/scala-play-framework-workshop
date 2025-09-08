package persistence

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.play.PlaySpec
import play.api.test.Helpers._
import play.api.test.WithApplication
import slick.jdbc.SQLServerProfile.api._

class DatabaseServiceSpec extends PlaySpec with ScalaFutures {

  implicit val defaultPatience: PatienceConfig = PatienceConfig(timeout = Span(5, Seconds), interval = Span(100, Millis))

  "DatabaseService" should {
    "successfully connect to database" in new WithApplication {
      val dbService = app.injector.instanceOf[IDatabaseService]
      
      // Basit bir test query çalıştır
      val testQuery = sql"SELECT 1 as test".as[Int]
      
      whenReady(dbService.run(testQuery)) { result =>
        result mustBe Seq(1)
      }
    }
    
    "handle database operations correctly" in new WithApplication {
      val dbService = app.injector.instanceOf[IDatabaseService]
      
      // Test transaction
      val testAction = DBIO.seq(
        sql"SELECT 1".as[Int],
        sql"SELECT 2".as[Int]
      )
      
      whenReady(dbService.run(testAction)) { _ =>
        // Eğer buraya geldiyse, database connection başarılı
        succeed
      }
    }
  }
}

