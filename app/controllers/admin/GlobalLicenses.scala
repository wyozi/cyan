package controllers.admin

import javax.inject.Inject

import auth.Authentication
import play.api.mvc.{AbstractController, BodyParsers, ControllerComponents}

import scala.concurrent.ExecutionContext

class GlobalLicenses @Inject()
  (cc: ControllerComponents,
   auth: Authentication,
   template: views.html.admin.license_view)
  (implicit ec: ExecutionContext, parser: BodyParsers.Default) extends AbstractController(cc) {
    def viewLicense(license: String) = auth.adminOnly { _ =>
      Ok(template(license))
    }
}
