package controllers.admin

import javax.inject.Inject

import auth.Secured
import cyan.backend.Backend
import dao._
import play.api.mvc.{BodyParsers, Controller}

import scala.concurrent.ExecutionContext

class GlobalLicenses @Inject() (template: views.html.admin.license_view)(
  implicit ec: ExecutionContext,
  parser: BodyParsers.Default
) extends Controller with Secured {
    def viewLicense(license: String) = SecureAction {
      Ok(template(license))
    }
}
