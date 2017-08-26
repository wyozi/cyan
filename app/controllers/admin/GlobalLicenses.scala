package controllers.admin

import javax.inject.Inject

import auth.Secured
import cyan.backend.Backend
import dao._
import play.api.mvc.{BodyParsers, Controller}

import scala.concurrent.ExecutionContext

class GlobalLicenses @Inject() (
  implicit ec: ExecutionContext,
  backend: Backend,
  productsDAO: ProductsDAO,
  plpDAO: ProdLicensePingDAO,
  productConfigDAO: ProductConfigDAO,
  pingsDAO: PingsDAO,
  pingExtrasDAO: PingExtrasDAO,
  responsesDAO: ResponsesDAO,
  parser: BodyParsers.Default
) extends Controller with Secured {
    def viewLicense(license: String) = SecureAction {
      Ok(views.html.admin.license_view(license))
    }
}
