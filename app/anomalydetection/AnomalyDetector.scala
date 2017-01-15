package anomalydetection

import scala.concurrent.Future

/**
  * Created by wyozi on 3.2.2016.
  */
abstract class AnomalyDetector {
  def name: String

  /**
    * @return identifier of this anomaly detector. Must be unique
    */
  def id: String

  def detectAnomalies(): Future[List[Anomaly]]
}
