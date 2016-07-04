package anomalydetection.impl

import anomalydetection.{Anomaly, AnomalyDetector, Medium}
import com.google.inject.Inject
import dao.anomalydet.MUOLAnomalyDAO
import org.joda.time.LocalDateTime
import play.api.mvc.Call

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by wyozi on 3.2.2016.
  */
class ManyUsersOneLicense @Inject() (muolLAnomalyDAO: MUOLAnomalyDAO) extends AnomalyDetector {
  val USER_PER_LICENSE_THRESHOLD = 5
  val DAY_THRESHOLD = 2

  override def name: String = "Many users on license"

  override def detectAnomalies(): Future[List[Anomaly]] = {
    muolLAnomalyDAO.findDistinctUserGroups(USER_PER_LICENSE_THRESHOLD, LocalDateTime.now().minusDays(DAY_THRESHOLD)).map(mars => mars.map {
      case (prod, license, userCount) => new MUOLAnomaly(prod.name, prod.id, license, userCount)
    }.toList)
  }

  class MUOLAnomaly(productName: String, productId: Int, license: String, distinctUserCount: Int) extends Anomaly(Medium) {
    override def relatedLinks: List[(String, Call)] = List(
      ("product", controllers.admin.prod.routes.Products.view(productId)),
      ("license", controllers.admin.prod.routes.ProductLicenses.licenseView(productId, license))
    )

    override def toShortString: String = s"$distinctUserCount distinct users on a single license of $productName"
  }
}
