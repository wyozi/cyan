package anomalydetection

/**
  * Created by wyozi on 4.2.2016.
  */
sealed class AnomalySeverity

case object Low extends AnomalySeverity
case object Medium extends AnomalySeverity
case object Significant extends AnomalySeverity
case object Critical extends AnomalySeverity
