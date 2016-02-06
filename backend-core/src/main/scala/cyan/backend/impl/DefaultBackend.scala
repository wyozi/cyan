package cyan.backend.impl

import cyan.backend.Backend
import play.twirl.api.Html

/**
  * Created by wyozi on 6.2.2016.
  */
class DefaultBackend extends Backend {
  override def createUserPopupHTML(user: String): Option[Html] = None
  override def createLicensePopupHTML(license: String): Option[Html] = None
}
