package controllers.admin

import auth.Secured
import com.google.inject.Inject
import controllers.admin.anomalydetection.ManyUsersOneLicense
import dao.ProductsDAO
import play.api.mvc.Controller

class Anomalies @Inject() (implicit productsDAO: ProductsDAO) extends Controller with Secured {
  def overview = SecureAction {
    Ok(views.html.admin_anomaly_overview())
  }
}
object Anomalies {
  val activeDetections = List(ManyUsersOneLicense)
}