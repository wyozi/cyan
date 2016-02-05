package dbrepo

import anorm._
import model.Response
import play.api.db.DB

/**
  * Created by wyozi on 5.2.2016.
  */
class ResponseRepository {
  def getUnregisteredProdLicenseResponse(productId: Int): Option[Response] = {
    import play.api.Play.current

    DB.withConnection { implicit c =>
      SQL(
        """
          |SELECT * FROM Products
          |JOIN Responses ON Products.defaultresp_unreg = Responses.id
          |WHERE Products.id = {productId}
        """.stripMargin)
        .on('productId -> productId)
        .as(Response.Parser.singleOpt)
    }
  }
}
