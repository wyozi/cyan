package controllers.admin.prod

import auth.Authentication
import com.google.inject.Inject
import dao._
import model.ProductLicense
import play.api.data.Form
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by wyozi on 16.2.2016.
  */
class ProductLicenses @Inject()
  (val cc: ControllerComponents,
   auth: Authentication,
   viewTemplate: views.html.admin.prod_license_view,
   productsDAO: ProductsDAO,
   pingResponsesDAO: PingResponsesDAO)
  (implicit parser: BodyParsers.Default) extends AbstractController(cc) {

  def licenseView(prodId: Int, licenseId: String): Action[AnyContent] = auth.adminOnlyAsync { implicit request =>
    productsDAO.findById(prodId).map {
      case Some(prod) =>
        Ok(viewTemplate(ProductLicense(prod, licenseId)))
    }
  }


  import play.api.data.Forms._
  val licenseResponseForm = Form("response" -> optional(number))
  def setProductLicenseResponse(productId: Int, license: String): Action[AnyContent] = auth.adminOnlyAsync { implicit request =>
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
