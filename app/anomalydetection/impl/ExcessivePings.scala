package anomalydetection.impl

import anomalydetection.{Anomaly, AnomalyDetector}

import scala.concurrent.Future

/**
  * Created by wyozi on 4.2.2016.
  */
class ExcessivePings extends AnomalyDetector {
  override def name: String = "Excessive pings"

  override def detectAnomalies(): Future[List[Anomaly]] = Future.successful(Nil)
}
