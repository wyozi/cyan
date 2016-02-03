package controllers.admin

import auth.Secured
import controllers.admin.anomalydetection.MultiLicenseUser
import play.api.mvc.Controller

object Anomalies extends Controller with Secured {
  val activeDetections = List(MultiLicenseUser)

  def overview = SecureAction {
    Ok(":D")
  }
}