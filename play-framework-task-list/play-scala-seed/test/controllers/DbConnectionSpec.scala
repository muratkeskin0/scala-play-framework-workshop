package controllers

import org.scalatest.wordspec.AsyncWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterAll
import slick.jdbc.SQLServerProfile.api._

class DbConnectionSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  // application.conf içindeki "slick.dbs.default.db" ayarını kullanır
  private val db = Database.forConfig("slick.dbs.default.db")

  "Database" should {
    "connect and respond to SELECT 1" in {
      db.run(sql"SELECT 1".as[Int]).map { rows =>
        rows should contain (1)     // List(1) bekliyoruz
      }
    }
  }

  // Test tamamlanınca havuzu kapatalım
  override def afterAll(): Unit = {
    db.close()
    super.afterAll()
  }
}
