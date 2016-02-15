package controllers.admin.prod

import auth.Secured
import com.google.inject.Inject
import dao._
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by wyozi on 16.2.2016.
  */
class ProductPingExtras @Inject() ()
  (implicit pingResponsesDAO: PingResponsesDAO,
    responsesDAO: ResponsesDAO,
    plpDAO: ProdLicensePingDAO,
    pingExtrasDAO: PingExtrasDAO,
    productsDAO: ProductsDAO) extends Controller with Secured {

  def view(prodId: Int, pingExtra: String) = SecureAction.async {
    productsDAO.findById(prodId).map {
      case Some(prod) => Ok(views.html.admin_prod_pingextra_view(prod, pingExtra))
    }
  }

}
