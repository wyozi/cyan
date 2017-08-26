package controllers.admin

import javax.inject.Inject

import auth.Secured
import cyan.backend.Backend
import dao._
import play.api.mvc.{BodyParsers, Controller}

import scala.concurrent.ExecutionContext

class IPs @Inject() (implicit backend: Backend, productsDAO: ProductsDAO, productConfigDAO: ProductConfigDAO, pingsDAO: PingsDAO, pingExtrasDAO: PingExtrasDAO, parser: BodyParsers.Default, ec: ExecutionContext, responsesDAO: ResponsesDAO) extends Controller with Secured {
  def viewIp(ip: String) = SecureAction {
    Ok(views.html.admin.ip_view(ip))
  }
}
