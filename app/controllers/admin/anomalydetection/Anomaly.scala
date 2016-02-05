package controllers.admin.anomalydetection

/**
  * Created by wyozi on 4.2.2016.
  */
case class Anomaly(name: String, severity: AnomalySeverity) {
  def toShortString: String = s"$name [${severity.getClass.getSimpleName}]"
}