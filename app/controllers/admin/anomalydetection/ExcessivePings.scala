package controllers.admin.anomalydetection

/**
  * Created by wyozi on 4.2.2016.
  */
object ExcessivePings extends AnomalyDetector {
  override def name: String = "Excessive pings"

  override def detectAnomalies(): List[Anomaly] = Nil
}
