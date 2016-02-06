package controllers.admin

import auth.Secured
import controllers.admin.anomalydetection.ManyUsersOneLicense
import play.api.mvc.Controller

class Anomalies extends Controller with Secured {
  def overview = SecureAction {
    Ok(views.html.admin_anomaly_overview())
  }
}
object Anomalies {
  val activeDetections = List(ManyUsersOneLicense)
}