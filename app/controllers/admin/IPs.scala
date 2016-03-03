package controllers.admin

import javax.inject.Inject

import auth.Secured
import cyan.backend.Backend
import dao._
import play.api.mvc.Controller

class IPs @Inject() (implicit backend: Backend, productsDAO: ProductsDAO, productConfigDAO: ProductConfigDAO, pingsDAO: PingsDAO, pingExtrasDAO: PingExtrasDAO, responsesDAO: ResponsesDAO) extends Controller with Secured {
  def viewIp(ip: String) = SecureAction {
    Ok(views.html.admin.ip_view(ip))
  }
}
