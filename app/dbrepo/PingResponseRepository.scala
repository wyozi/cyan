package dbrepo

import anorm._
import model.Response
import play.api.db.DB

/**
  * Created by wyozi on 5.2.2016.
  */
class PingResponseRepository {
  def getResponse(productId: Option[Int], license: Option[String], user: Option[String]): Option[Response] = {
    import play.api.Play.current
    DB.withConnection { implicit connection =>
      SQL(
        """
          |SELECT *
          |FROM Responses
          |WHERE id = (
          |  SELECT response FROM PingResponses WHERE (productId = {productId} AND licenseId = {license}) AND userId = {user}
          |  UNION ALL
          |  SELECT response FROM PingResponses WHERE userId = {user}
          |  UNION ALL
          |  SELECT response FROM PingResponses WHERE (productId = {productId} AND licenseId = {license})
          |  UNION ALL
          |  SELECT response FROM PingResponses WHERE productId = {productId}
          |  LIMIT  1
          |)
        """.stripMargin)
        .on('productId -> productId.get)
        .on('license -> license.get)
        .on('user -> user)
        .as(Response.Parser.singleOpt)
    }
  }
}
