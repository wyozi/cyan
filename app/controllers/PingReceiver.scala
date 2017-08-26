package controllers

import javax.inject.Inject

import dao._
import model.PingExtra
import play.api.mvc._
import util.PingCheckDSL

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by wyozi on 3.2.2016.
  */
class PingReceiver @Inject() (productsDAO: ProductsDAO,
  pingsDAO: PingsDAO,
  responsesDAO: ResponsesDAO,
  pingResponsesDAO: PingResponsesDAO,
  pingExtrasDAO: PingExtrasDAO,
  pingCheckDSL: PingCheckDSL) extends Controller {

  private case class ProductNotFound() extends Exception("product not found")

  def registerPing(req: Request[AnyContent], product: String, license: String, user: String, extras: Map[String, String]): Future[Result] = {
    productsDAO
      .findByShortName(product)
      .flatMap {
        case Some(x) => Future.successful(x)
        case _ => Future.failed(ProductNotFound())
      }
      .flatMap { prod => // product to (product, response)
          pingResponsesDAO.getBestResponse(Some(prod.id), Some(license), Some(user)).map(resp => (prod, resp))
      }
      .flatMap {
        case (prod, resp) => pingCheckDSL.check(prod, license, user, extras, resp).map {
          case None => (prod, resp)
          case _ => ??? // XXX response modification not yet added in
        }
      }
      .flatMap { // (prod, resp) to (pingId, resp)
        case (prod, resp) => pingsDAO.insert(product, license, user, req.remoteAddress, resp.map(_.id)).map(pingId => (pingId, resp))
      }
      .map { // (pingId, resp) to resp
        case (pingId, response) =>
          for ((key, value) <- extras) {
            pingExtrasDAO.insert(PingExtra(pingId, key, value))
          }
          response
      }
      .map(resp => Ok(resp.map(_.body).getOrElse("")))
  }

  def ping = Action.async { req =>
    req.body.asFormUrlEncoded
        .map { params => // unpack user, license, prod, extras into a tuple
          (
            params.get("user").map(_.head.mkString),
            params.get("license").map(_.head.mkString),
            params.get("prod").map(_.head.mkString),
            params
              .filterKeys(k => k.startsWith("x_"))
              .map { case (key, values) => (key.substring(2), values.head.mkString) }
          )
        }
    match {
      case Some((Some(user), Some(license), Some(product), extras)) =>
        registerPing(req, product, license, user, extras)
          .fallbackTo(Future.successful(BadRequest(""))) // TODO log this
      case _ => Future.successful(BadRequest("")) // TODO log this
    }
  }

}