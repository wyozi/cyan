package controllers.admin

import javax.inject.Inject

import auth.Authentication
import cyan.backend.Backend
import dao._
import play.api.mvc._

import scala.concurrent.ExecutionContext

class Users @Inject()
  (cc: ControllerComponents,
   auth: Authentication,
   viewTemplate: views.html.admin.user_view)
  (implicit backend: Backend, productsDAO: ProductsDAO, parser: BodyParsers.Default, plpDAO: ProdLicensePingDAO, productConfigDAO: ProductConfigDAO, pingsDAO: PingsDAO, pingExtrasDAO: PingExtrasDAO, responsesDAO: ResponsesDAO, ec: ExecutionContext) extends AbstractController(cc) {

  def viewUser(user: String): Action[AnyContent] = auth.adminOnly { _ =>
    Ok(viewTemplate(user))
  }
}
