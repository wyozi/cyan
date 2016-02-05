import dbrepo.PingResponseRepository
import model.Response
import play.api.db.{Database, Databases}
import play.api.db.evolutions.Evolutions

/**
  * Created by wyozi on 5.2.2016.
  */
object DbUtils {
  def rawDatabase() = Databases.inMemory(urlOptions = Map("MODE" -> "PostgreSQL"))

  def newDatabase(
    applyEvolutions: Boolean = true) = {

    val db = rawDatabase()

    if (applyEvolutions) Evolutions.applyEvolutions(db)


    db
  }

  implicit class TestDatabase(val db: Database) {
    import anorm._

    /**
      * @return inserted response id
      */
    def insertResponse(name: String, body: String): Int = {
      db.withConnection { implicit c =>
        SQL"INSERT INTO Responses(name, response) VALUES(${name}, ${body})"
          .executeInsert()
      }.get.toInt
    }

    /**
      * @return inserted ping response id
      */
    def insertPingResponse(productId: Option[Int], license: Option[String], user: Option[String], responseId: Option[Int]): Int = {
      db.withConnection { implicit c =>
        SQL"INSERT INTO PingResponses(product_id, license, user_name, response_id) VALUES($productId, $license, $user, $responseId)"
          .executeInsert()
      }.get.toInt
    }

    def truncate() = {
      db.withConnection { implicit c =>
        SQL"TRUNCATE TABLE PingResponses"
          .execute()
      }
    }
  }
}
