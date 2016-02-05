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
          |SELECT * FROM PingResponses pr
          |LEFT JOIN Responses ON pr.response = Responses.id
          |WHERE pr.response IS NOT NULL AND pr.userId = {user} AND pr.licenseId = {license} AND pr.productId = {productId}
        """.stripMargin)
        .on('productId -> productId.get)
        .on('user -> user.get)
        .on('license -> license.get)
        .as(Response.Parser.singleOpt)
    }
  }
}
