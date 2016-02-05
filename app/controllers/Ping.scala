package controllers

import javax.inject.Inject

import anorm.SqlParser._
import anorm._
import controllers.Application._
import play.api.db.DB
import play.api.mvc.{Result, Action, Controller}
import response.{ResponseFindParameters, ResponseFinder}

/**
  * Created by wyozi on 3.2.2016.
  */
class Ping @Inject() (responseFinder: ResponseFinder) extends Controller {

  def registerPing(product: String, license: String, user: String): Result = {
    import play.api.Play.current
    DB.withConnection { implicit connection =>
      val productIdOpt = SQL("SELECT id FROM Products WHERE shortName = {shortName}")
        .on('shortName -> product)
        .as(int("id").singleOpt)

      if (productIdOpt.isEmpty) {
        // product not found but we don't want to reveal that to pinger so return nil ok
        Ok("")
      } else {
        val productId = productIdOpt.get

        // Get a response from responseFinder
        val response = responseFinder.find(ResponseFindParameters(productIdOpt, Some(license), Some(user)))

        import anorm._
        import anorm.SqlParser._

        // Insert ping into db
        SQL("INSERT INTO Pings(userId, licenseId, product, responseId) VALUES ({user}, {license}, {product}, {responseId})")
          .on('user -> user)
          .on('license -> license)
          .on('product -> product)
          .on('responseId -> response.map(_.id))
          .executeInsert()

        Ok(response.map(_.body).getOrElse(""))
      }
    }
  }

  def ping = Action { req =>
    val params = req.body.asFormUrlEncoded.get

    val user = params.get("user").map(_.head.mkString)
    val license = params.get("license").map(_.head.mkString)
    val product = params.get("prod").map(_.head.mkString)

    if (user.isEmpty || license.isEmpty || product.isEmpty) {
      Ok("") // TODO log this somehow
    } else {
      registerPing(product.get, license.get, user.get)
    }
  }

}