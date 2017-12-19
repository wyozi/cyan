package controllers.admin

import auth.Authentication
import com.google.inject.Inject
import cyan.backend.{Backend, Query}
import dao.{ProductConfigDAO, ProductsDAO}
import play.api.mvc.{AbstractController, BodyParsers, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

class BackendController @Inject()
  (cc: ControllerComponents,
   auth: Authentication)
  (
    implicit backend: Backend,
    productsDAO: ProductsDAO,
    productConfigDAO: ProductConfigDAO,
    parser: BodyParsers.Default,
    ec: ExecutionContext
  ) extends AbstractController(cc) {
  def view(query: String, productId: Option[Int], license: Option[String]) = auth.adminOnlyAsync { req =>
    productId.map(id => productsDAO.findById(id)).getOrElse(Future.successful(None)).flatMap { prod =>
      val backendProduct = prod.map(_.backend())
      val backendLicense = (prod, license) match {
        case (Some(sprod), Some(slicense)) => Some(model.backendLicense(sprod, slicense))
        case _ => None
      }

      val params = req.queryString.mapValues(_.head)

      backend.respondToQuery(Query(query, params, backendProduct, backendLicense)).map {
        case Some(html) => Ok(html)
        case _ => NotFound
      }
    }
  }
}
