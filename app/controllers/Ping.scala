package controllers

import javax.inject.Inject

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import play.api.mvc._
import response.{ResponseFindParameters, ResponseFinder}

/**
  * Created by wyozi on 3.2.2016.
  */
class Ping @Inject() (responseFinder: ResponseFinder) extends Controller {

  def registerPing(req: Request[AnyContent], product: String, license: String, user: String, extras: Map[String, String]): Result = {
    import play.api.Play.current
    DB.withConnection { implicit connection =>
      val productIdOpt = SQL("SELECT id FROM Products WHERE short_name = {shortName}")
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

        // Insert ping into db
        val pingId = SQL("INSERT INTO Pings(product, license, user_name, ip, response_id) VALUES ({product}, {license}, {user}, {ip}, {responseId})")
          .on('product -> product)
          .on('license -> license)
          .on('user -> user)
          .on('ip -> req.remoteAddress)
          .on('responseId -> response.map(_.id))
          .executeInsert()
          .get

        val q = SQL("INSERT INTO PingExtras (ping_id, key, value) VALUES ({pingId}, {key}, {value})")
            .on('pingId -> pingId)

        for ((key, value) <- extras) {
          q
            .on('key -> key, 'value -> value)
            .executeInsert()
        }

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
      val extras = params
        .filterKeys(k => k.startsWith("x_"))
        .map { case (key, values) => (key.substring(2), values.head.mkString) }
      registerPing(req, product.get, license.get, user.get, extras)
    }
  }

}