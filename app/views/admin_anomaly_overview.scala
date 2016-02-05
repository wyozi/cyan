package views

import controllers.admin.anomalydetection._

/**
  * Created by wyozi on 5.2.2016.
  */
object admin_anomaly_overview {
  def getSeverityBootstrapClass(severity: AnomalySeverity): String = severity match {
    case Low => "active"
    case Significant => "warning"
    case Critical => "danger"
    case _ => ""
  }
}
