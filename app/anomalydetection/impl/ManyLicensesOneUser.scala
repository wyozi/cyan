package anomalydetection.impl

import anomalydetection.{Anomaly, AnomalyDetector, Significant}
import com.google.inject.Inject
import dao.anomalydet.MLOUAnomalyDAO
import play.api.mvc.Call

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ManyLicensesOneUser @Inject() (anomalyDAO: MLOUAnomalyDAO) extends AnomalyDetector {
  val LICENSE_PER_IP_THRESHOLD = 2

  override def name: String = "Many licenses on one ip"

  override def detectAnomalies(): Future[List[Anomaly]] = {
    anomalyDAO.findIpLicenseCount(LICENSE_PER_IP_THRESHOLD).map(pic => pic.map {
      case (prod, ip, licenseCount) => new MLOUAnomaly(prod, ip, licenseCount)
    }.toList)
  }

  class MLOUAnomaly(product: model.Product, user: String, distinctLicenseCount: Int) extends Anomaly(Significant) {
    override def relatedLinks: List[(String, Call)] = List(
      ("product", controllers.admin.prod.routes.Products.view(product.id))
    )

    override def toShortString: String = s"$distinctLicenseCount distinct licenses on a single user $user"
  }
}
