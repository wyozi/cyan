package controllers.admin

import auth.Secured
import com.google.inject.Inject
import cyan.backend.{Backend, Query}
import dao.{ProductConfigDAO, ProductsDAO}
import play.api.mvc.Controller

import scala.collection.immutable.HashMap
import scala.concurrent.{Future, ExecutionContext}

class BackendController @Inject() (implicit backend: Backend, productsDAO: ProductsDAO, productConfigDAO: ProductConfigDAO, ec: ExecutionContext) extends Controller with Secured {
  def view(query: String, productId: Option[Int], license: Option[String]) = SecureAction.async {
    productId.map(id => productsDAO.findById(id)).getOrElse(Future.successful(None)).flatMap { prod =>
      val backendProduct = prod.map(_.backend())
      val backendLicense = (prod, license) match {
        case (Some(sprod), Some(slicense)) => Some(model.backendLicense(sprod, slicense))
        case _ => None
      }

      backend.respondToQuery(Query(query, HashMap(), backendProduct, backendLicense)).map {
        case Some(html) => Ok(html)
        case _ => NotFound
      }
    }
  }
}
