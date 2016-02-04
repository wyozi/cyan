package controllers.admin

import auth.Secured
import controllers.admin.anomalydetection.ManyUsersOneLicense
import play.api.mvc.Controller

object Anomalies extends Controller with Secured {
  val activeDetections = List(ManyUsersOneLicense)

  def overview = SecureAction {
    Ok(":D")
  }
}