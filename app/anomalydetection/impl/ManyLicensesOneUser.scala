package anomalydetection.impl

import anomalydetection.{Anomaly, AnomalyDetector, Significant}
import com.google.inject.Inject
import dao.anomalydet.MLOUAnomalyDAO
import play.api.mvc.Call

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ManyLicensesOneUser @Inject() (anomalyDAO: MLOUAnomalyDAO) extends AnomalyDetector {
  val LICENSE_PER_USER_THRESHOLD = 2

  override def name: String = "Many licenses by one user"

  override def id: String = "mlou"

  override def detectAnomalies(): Future[List[Anomaly]] = {
    anomalyDAO.findUserLicenseCount(LICENSE_PER_USER_THRESHOLD).map(pic => pic.map {
      case (prod, user, licenseCount) => new MLOUAnomaly(prod, user, licenseCount)
    }.filter(a => !ManyLicensesOneUser.isFiltered(a.user)).toList)
  }

  class MLOUAnomaly(product: model.Product, val user: String, distinctLicenseCount: Int) extends Anomaly(Significant) {
    override def relatedLinks: List[(String, Call)] = List(
      ("product", controllers.admin.prod.routes.Products.view(product.id)),
      ("user", controllers.admin.routes.Users.viewUser(user))
    )

    override def toShortString: String = s"User $user has $distinctLicenseCount distinct licenses on ${product.name}"
  }
}
object ManyLicensesOneUser {

  val IP_FILTER = Set(
    "0.0.0.0"
  )
  val IpPortRegex = "^(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}):(\\d{1,5})$".r

  /**
    * Filters out users that are known to be often invalid.
    * At the moment only implements "IP:PORT" user filter.
    * TODO: make adding filters dynamically
    */
  def isFiltered(user: String): Boolean = {
    user match {
      case IpPortRegex(ip, port) => IP_FILTER.contains(ip)
      case _ => false
    }
  }
}