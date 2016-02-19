package controllers.admin

import auth.Secured
import com.google.inject.Inject
import anomalydetection.AnomalyDetector
import dao.ProductsDAO
import play.api.mvc.Controller

class Anomalies @Inject() (val detections: java.util.Set[AnomalyDetector])(implicit productsDAO: ProductsDAO) extends Controller with Secured {
  def overview = SecureAction {
    Ok(views.html.admin.anomaly_overview(detections))
  }
}