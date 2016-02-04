package controllers.admin.anomalydetection

/**
  * Created by wyozi on 3.2.2016.
  */
abstract class AnomalyDetector {
  def name: String
  def detectAnomalies(): List[Anomaly]
}
