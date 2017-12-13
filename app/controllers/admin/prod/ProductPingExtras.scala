package controllers.admin.prod

import auth.Secured
import com.google.inject.Inject
import cyan.backend.Backend
import dao._
import play.api.mvc.{BodyParsers, Controller}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by wyozi on 16.2.2016.
  */
class ProductPingExtras @Inject() ()
  (implicit backend: Backend,
    pingResponsesDAO: PingResponsesDAO,
    responsesDAO: ResponsesDAO,
    plpDAO: ProdLicensePingDAO,
    productConfigDAO: ProductConfigDAO,
    pingsDAO: PingsDAO,
    pingExtrasDAO: PingExtrasDAO,
    parser: BodyParsers.Default,
    productsDAO: ProductsDAO) extends Controller with Secured {

  def list(prodId: Int) = SecureAction.async {
    productsDAO.findById(prodId).map {
      case Some(prod) => Ok(views.html.admin.prod_pingextra_list(prod))
    }
  }

  def view(prodId: Int, key: String, days: Int, value: Option[String]) = SecureAction.async { implicit request =>
    productsDAO.findById(prodId).map {
      case Some(prod) => Ok(value match {
        case Some(v) => views.html.admin.prod_pingextra_view_value(prod, key, v)
        case _ => views.html.admin.prod_pingextra_view(prod, key, days)
      })
    }
  }

}
