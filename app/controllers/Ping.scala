package controllers

import anorm.SqlParser._
import anorm._
import controllers.Application._
import play.api.db.DB
import play.api.mvc.{Action, Controller}

/**
  * Created by wyozi on 3.2.2016.
  */
object Ping extends Controller {

  def ping = Action { req =>
    val params = req.body.asFormUrlEncoded.get

    val userId = params.get("user").map(_.head.mkString).get
    val licenseId = params.get("license").map(_.head.mkString).get
    val product = params.get("prod").map(_.head.mkString).get

    import play.api.Play.current
    DB.withConnection { implicit connection =>
      val productIdOpt = SQL("SELECT id FROM Products WHERE shortName = {shortName}")
        .on('shortName -> product)
        .as(int("id").singleOpt)

      if (productIdOpt.isEmpty) {
        Ok("NOT FOUND !!")
      } else {
        val productId = productIdOpt.get

        import anorm._
        import anorm.SqlParser._

        SQL("INSERT INTO Pings(userId, licenseId, product) VALUES ({user}, {license}, {product})")
          .on('user -> userId)
          .on('license -> licenseId)
          .on('product -> product)
          .executeInsert()

        // First try to get specific response
        val responseOpt = SQL("""
              |SELECT Responses.response as response FROM PingResponses pr
              |LEFT JOIN Responses ON pr.response = Responses.id
              |WHERE pr.userId = {user} AND pr.licenseId = {license} AND pr.productId = {productId}
            """.stripMargin)
          .on('user -> userId)
          .on('license -> licenseId)
          .on('productId -> productId)
          .as(str("response").singleOpt)
        .orElse(
          SQL("""
                |SELECT Responses.response as response FROM Products
                |LEFT JOIN Responses ON Products.defaultresp_unreg = Responses.id
                |WHERE Products.id = {productId}
              """.stripMargin)
            .on('productId -> productId)
            .as(get[Option[String]]("response").singleOpt)
            .flatten
        )

        Ok(responseOpt.getOrElse(""))
      }
    }
  }

}