package controllers.admin

import auth.Secured
import controllers.anomalydetection.MultiLicenseUser
import play.api.mvc.Controller

object AdminAnomalies extends Controller with Secured {
  val activeDetections = List(MultiLicenseUser)

  def overview = SecureAction {
    Ok(":D")
  }
}