package response.impl

import anorm.SqlParser._
import anorm._
import model.Response
import play.api.db.DB
import response.{ResponseFindParameters, ResponseFinder}

/**
  * Created by wyozi on 5.2.2016.
  */
class DatabaseResponseFinder extends ResponseFinder {
  /**
    * Finds suitable response to given parameters.
    */
  override def find(params: ResponseFindParameters): Option[Response] = {

    import play.api.Play.current
    DB.withConnection { implicit connection =>
      SQL(
        """
          |SELECT * FROM PingResponses pr
          |LEFT JOIN Responses ON pr.response = Responses.id
          |WHERE pr.userId = {user} AND pr.licenseId = {license} AND pr.productId = {productId}
        """.stripMargin)
        .on('user -> params.user.get)
        .on('license -> params.license.get)
        .on('productId -> params.productId.get)
        .as(Response.Parser.singleOpt)
        .orElse(
          SQL(
            """
                |SELECT * FROM Products
                |LEFT JOIN Responses ON Products.defaultresp_unreg = Responses.id
                |WHERE Products.id = {productId}
              """.stripMargin)
            .on('productId -> params.productId.get)
            .as(Response.Parser.singleOpt)
        )
    }
  }
}
