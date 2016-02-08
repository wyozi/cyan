package controllers

import javax.inject.Inject

import dao._
import model.PingExtra
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by wyozi on 3.2.2016.
  */
class Ping @Inject() (productsDAO: ProductsDAO,
  pingsDAO: PingsDAO,
  responsesDAO: ResponsesDAO,
  pingResponsesDAO: PingResponsesDAO,
  pingExtrasDAO: PingExtrasDAO) extends Controller {

  def registerPing(req: Request[AnyContent], product: String, license: String, user: String, extras: Map[String, String]): Future[Result] = {
    productsDAO
      .findByShortName(product)
      .flatMap {
        case Some(prod) => responsesDAO.findById(prod.id).map(resp => (prod, resp))
      }
      .flatMap {
        case (prod, resp) =>
          pingsDAO.insert(product, license, user, req.remoteAddress, resp.map(_.id)).map(pingId => (pingId, resp))
      }
      .map {
        case (pingId, response) =>
          for ((key, value) <- extras) {
            pingExtrasDAO.insert(PingExtra(pingId, key, value))
          }
          response
      }
      .map(resp => Ok(resp.map(_.body).getOrElse("")))
  }

  def ping = Action.async { req =>
    val params = req.body.asFormUrlEncoded.get

    val user = params.get("user").map(_.head.mkString)
    val license = params.get("license").map(_.head.mkString)
    val product = params.get("prod").map(_.head.mkString)

    if (user.isEmpty || license.isEmpty || product.isEmpty) {
      Future.successful(Ok("")) // TODO log this somehow
    } else {
      val extras = params
        .filterKeys(k => k.startsWith("x_"))
        .map { case (key, values) => (key.substring(2), values.head.mkString) }
      registerPing(req, product.get, license.get, user.get, extras)
    }
  }

}