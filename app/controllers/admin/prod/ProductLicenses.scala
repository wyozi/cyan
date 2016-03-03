package controllers.admin.prod

import auth.Secured
import com.google.inject.Inject
import dao._
import model.ProductLicense
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by wyozi on 16.2.2016.
  */
class ProductLicenses @Inject() ()
  (implicit pingResponsesDAO: PingResponsesDAO,
    responsesDAO: ResponsesDAO,
    productConfigDAO: ProductConfigDAO,
    pingExtrasDAO: PingExtrasDAO,
    pingsDAO: PingsDAO,
    plpDAO: ProdLicensePingDAO,
    productsDAO: ProductsDAO) extends Controller with Secured {

  def licenseView(prodId: Int, licenseId: String) = SecureAction.async {
    productsDAO.findById(prodId).map {
      case Some(prod) =>
        Ok(views.html.admin.prod_license_view(ProductLicense(prod, licenseId)))
    }
  }

  def setProductLicenseResponse(productId: Int, license: String) = SecureAction { req =>
    val params = req.body.asFormUrlEncoded.get

    val response = params.get("response").map(_.head.mkString).get match {
      case "null" => Option.empty
      case x => Some(x.toInt)
    }

    pingResponsesDAO.upsertExactPingResponse(Some(productId), Some(license), None, response)
    Redirect(routes.ProductLicenses.licenseView(productId, license))
  }
}
