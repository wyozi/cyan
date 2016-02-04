package controllers.admin.anomalydetection

/**
  * Created by wyozi on 4.2.2016.
  */
sealed class AnomalySeverity

object Low extends AnomalySeverity
object Medium extends AnomalySeverity
object Significant extends AnomalySeverity
object Critical extends AnomalySeverity
