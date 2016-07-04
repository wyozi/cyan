package controllers.admin

import javax.inject.Inject

import auth.Secured
import cyan.backend.Backend
import dao._
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

class GlobalLicenses @Inject() (implicit ec: ExecutionContext, backend: Backend, productsDAO: ProductsDAO, plpDAO: ProdLicensePingDAO, productConfigDAO: ProductConfigDAO, pingsDAO: PingsDAO, pingExtrasDAO: PingExtrasDAO, responsesDAO: ResponsesDAO) extends Controller with Secured {
    def viewLicense(license: String) = SecureAction {
      Ok(views.html.admin.license_view(license))
    }
}
