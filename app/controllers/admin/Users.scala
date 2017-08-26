package controllers.admin

import javax.inject.Inject

import auth.Secured
import cyan.backend.Backend
import dao._
import play.api.mvc.{BodyParsers, Controller}

import scala.concurrent.ExecutionContext

class Users @Inject() (implicit backend: Backend, productsDAO: ProductsDAO, parser: BodyParsers.Default, plpDAO: ProdLicensePingDAO, productConfigDAO: ProductConfigDAO, pingsDAO: PingsDAO, pingExtrasDAO: PingExtrasDAO, responsesDAO: ResponsesDAO, ec: ExecutionContext) extends Controller with Secured {
  def viewUser(user: String) = SecureAction {
    Ok(views.html.admin.user_view(user))
  }
}
