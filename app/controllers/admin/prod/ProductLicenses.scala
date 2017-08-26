package controllers.admin.prod

import auth.Secured
import com.google.inject.Inject
import cyan.backend.Backend
import dao._
import model.ProductLicense
import play.api.data.Form
import play.api.mvc.{BodyParsers, Controller}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by wyozi on 16.2.2016.
  */
class ProductLicenses @Inject() ()
  (implicit backend: Backend,
    pingResponsesDAO: PingResponsesDAO,
    responsesDAO: ResponsesDAO,
    productConfigDAO: ProductConfigDAO,
    pingExtrasDAO: PingExtrasDAO,
    pingsDAO: PingsDAO,
    plpDAO: ProdLicensePingDAO,
    parser: BodyParsers.Default,
    productsDAO: ProductsDAO) extends Controller with Secured {

  def licenseView(prodId: Int, licenseId: String) = SecureAction.async { implicit request =>
    productsDAO.findById(prodId).map {
      case Some(prod) =>
        Ok(views.html.admin.prod_license_view(ProductLicense(prod, licenseId)))
    }
  }


  import play.api.data.Forms._
  val licenseResponseForm = Form("response" -> optional(number))
  def setProductLicenseResponse(productId: Int, license: String) = SecureAction.async { implicit request =>
    licenseResponseForm.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest("Error: " + formWithErrors.toString)), // TODO improve msg
      plResponse => {
        pingResponsesDAO
          .upsertExactPingResponse(Some(productId), Some(license), None, plResponse)
          .map { x =>
            Redirect(routes.ProductLicenses.licenseView(productId, license))
          }
      }
    )
  }
}
