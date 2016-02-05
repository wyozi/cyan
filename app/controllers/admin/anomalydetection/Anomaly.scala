package controllers.admin.anomalydetection

import play.api.mvc.Call

/**
  * Created by wyozi on 4.2.2016.
  */
abstract class Anomaly(val severity: AnomalySeverity) {
  /**
    * relatedLinks returns a list of tuples where the string is the link name
    * and the Call is where we're linking to
    */
  def relatedLinks: List[(String, Call)] = Nil

  /**
    * Returns small description which is displayed on anomaly overview page.
    * @return
    */
  def toShortString: String
}