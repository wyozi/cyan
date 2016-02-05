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

  /**
    * Gets the ping response id that best matches given parameters.
    * Checks following in order:
    *  - product/licence match and user match
    *  - user match
    *  - product/licence match (with null user)
    *  - product match (with null license and user)
    *
    * @param productId
    * @param license
    * @param user
    * @return
    */
  def getBestPingResponseId(productId: Option[Int], license: Option[String], user: Option[String]): Option[Int] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          |SELECT id FROM PingResponses WHERE (product_id = {productId} AND license = {license}) AND user_name = {user}
          |UNION ALL
          |SELECT id FROM PingResponses WHERE user_name = {user}
          |UNION ALL
          |SELECT id FROM PingResponses WHERE (product_id = {productId} AND license = {license}) AND user_name IS NULL
          |UNION ALL
          |SELECT id FROM PingResponses WHERE product_id = {productId} AND license IS NULL AND user_name IS NULL
          |LIMIT  1
        """.stripMargin)
        .on('productId -> productId)
        .on('license -> license)
        .on('user -> user)
        .as(int("id").singleOpt)
    }
  }

  /**
    * Gets the ping response that matches given parameters exactly.
    *
    * @param productId
    * @param license
    * @param user
    * @return
    */
  def getExactPingResponseId(productId: Option[Int], license: Option[String], user: Option[String]): Option[Int] = {
    val whereQuery = (productId, license, user) match {
      case (Some(p), Some(l), Some(u)) => "(product_id = {productId} AND license = {license}) AND user_name = {user}"
      case (None, None, Some(u)) => "user_name = {user}"
      case (Some(p), Some(l), None) => "(product_id = {productId} AND license = {license}) AND user_name IS NULL"
      case (Some(p), None, None) => "product_id = {productId} AND license IS NULL AND user_name IS NULL"
      case params => throw new RuntimeException(s"cannot search for exact ping response with this list of params: $params")
    }
    val fullQuery = s"SELECT id FROM PingResponses WHERE $whereQuery LIMIT 1"

    DB.withConnection { implicit connection =>
      SQL(fullQuery)
        .on('productId -> productId)
        .on('license -> license)
        .on('user -> user)
        .as(int("id").singleOpt)
    }
  }

  private def getResponse(pingResponseId: Int): Option[Response] = {
      DB.withConnection { implicit connection =>
        SQL(
          """
            |SELECT *
            |FROM Responses
            |WHERE id = (SELECT response_id FROM PingResponses WHERE id = {prid})
            |LIMIT 1
          """.stripMargin)
          .on('prid -> pingResponseId)
          .as(Response.Parser.singleOpt)
      }
  }

  def getBestResponse(productId: Option[Int], license: Option[String], user: Option[String]): Option[Response] = {
    getBestPingResponseId(productId, license, user).flatMap(getResponse)
  }
  def getExactResponse(productId: Option[Int], license: Option[String], user: Option[String]): Option[Response] = {
    getExactPingResponseId(productId, license, user).flatMap(getResponse)
  }

  def upsertExactPingResponse(productId: Option[Int], license: Option[String], user: Option[String], response: Option[Int]): Unit = {
    DB.withConnection { implicit connection =>
      getExactPingResponseId(productId, license, user).fold(
        // empty
        SQL("INSERT INTO PingResponses(product_id, license, user_name, response_id) VALUES ({prod}, {license}, {user}, {resp})")
          .on('prod -> productId, 'license -> license, 'user -> user, 'resp -> response)
          .execute()
      ) { id =>
        // found
        SQL("UPDATE PingResponses SET response_id = {resp} WHERE id = {prid}")
          .on('prid -> id, 'resp -> response)
          .execute()
      }
    }
  }
}
