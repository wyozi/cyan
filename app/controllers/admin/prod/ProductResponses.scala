package controllers.admin.prod

import auth.Secured
import com.google.inject.Inject
import dao._
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

class ProductResponses @Inject() ()
  (implicit ec: ExecutionContext,
    pingsDAO: PingsDAO,
    pingResponsesDAO: PingResponsesDAO,
    responsesDAO: ResponsesDAO,
    productConfigDAO: ProductConfigDAO,
    pingExtrasDAO: PingExtrasDAO,
    plpDAO: ProdLicensePingDAO,
    productsDAO: ProductsDAO) extends Controller with Secured {

  def list(prodId: Int) = SecureAction.async {
    productsDAO.findById(prodId).map {
      case Some(prod) =>
        Ok(views.html.admin.prod_resp_list(prod))
    }
  }
}

