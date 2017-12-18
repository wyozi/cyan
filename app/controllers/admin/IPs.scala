package controllers.admin

import javax.inject.Inject

import auth.Secured
import cyan.backend.Backend
import dao._
import play.api.mvc.{BodyParsers, Controller}

import scala.concurrent.ExecutionContext

class IPs @Inject() (viewTemplate: views.html.admin.ip_view)(implicit ec: ExecutionContext, parser: BodyParsers.Default) extends Controller with Secured {
  def viewIp(ip: String) = SecureAction {
    Ok(viewTemplate(ip))
  }
}
