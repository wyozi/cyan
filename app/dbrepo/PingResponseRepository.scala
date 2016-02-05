package dbrepo

import anorm._
import anorm.SqlParser._
import model.Response
import play.api.db.DB

/**
  * Created by wyozi on 5.2.2016.
  */
class PingResponseRepository {
  import play.api.Play.current

  def getPingResponseId(productId: Option[Int], license: Option[String], user: Option[String]): Option[Int] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          |SELECT id FROM PingResponses WHERE (productId = {productId} AND licenseId = {license}) AND userId = {user}
          |UNION ALL
          |SELECT id FROM PingResponses WHERE userId = {user}
          |UNION ALL
          |SELECT id FROM PingResponses WHERE (productId = {productId} AND licenseId = {license})
          |UNION ALL
          |SELECT id FROM PingResponses WHERE productId = {productId}
          |LIMIT  1
        """.stripMargin)
        .on('productId -> productId.get)
        .on('license -> license.get)
        .on('user -> user)
        .as(int("id").singleOpt)
    }
  }

  def getResponse(productId: Option[Int], license: Option[String], user: Option[String]): Option[Response] = {
    getPingResponseId(productId, license, user)
        .flatMap { id =>
          DB.withConnection { implicit connection =>
            SQL(
              """
                |SELECT *
                |FROM Responses
                |WHERE id = (SELECT response FROM PingResponses WHERE id = {prid})
              """.stripMargin)
              .on('prid -> id)
              .as(Response.Parser.singleOpt)
          }
        }
  }

  def upsertPingResponse(productId: Option[Int], license: Option[String], user: Option[String], response: Option[Int]): Unit = {
    DB.withConnection { implicit connection =>
      getPingResponseId(productId, license, user).fold(
        // empty
        SQL("INSERT INTO PingResponses(productId, licenseId, userId, response) VALUES ({prod}, {license}, {user}, {resp})")
          .on('prod -> productId, 'license -> license, 'user -> user, 'resp -> response)
          .execute()
      ) { id =>
        // found
        SQL("UPDATE PingResponses SET response = {resp} WHERE id = {prid}")
          .on('prid -> id, 'resp -> response)
          .execute()
      }
    }
  }
}
