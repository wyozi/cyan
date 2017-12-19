package controllers.admin

import javax.inject.Inject

import auth.Authentication
import play.api.mvc.{AbstractController, BodyParsers, ControllerComponents}

import scala.concurrent.ExecutionContext

class IPs @Inject()
  (cc: ControllerComponents,
   auth: Authentication,
   viewTemplate: views.html.admin.ip_view)
  (implicit ec: ExecutionContext, parser: BodyParsers.Default) extends AbstractController(cc) {
  def viewIp(ip: String) = auth.adminOnly { _ =>
    Ok(viewTemplate(ip))
  }
}
