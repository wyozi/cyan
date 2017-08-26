package controllers.admin

import anomalydetection.AnomalyDetector
import auth.Secured
import com.google.inject.Inject
import dao.ProductsDAO
import play.api.libs.json.Json
import play.api.mvc.{BodyParsers, Controller}

import scala.concurrent.{ExecutionContext, Future}

class Anomalies @Inject() (val detections: java.util.Set[AnomalyDetector])(
  implicit productsDAO: ProductsDAO,
  parser: BodyParsers.Default,
  ex: ExecutionContext
) extends Controller with Secured {
  def overview = SecureAction {
    Ok(views.html.admin.anomaly_overview(detections))
  }

  def fetch(anomalyId: String) = SecureAction.async { req =>
    import scala.collection.JavaConversions._
    detections.find(_.id == anomalyId) match {
      case Some(det) =>
        det.detectAnomalies().map(anomalies => {
          Ok(Json.obj(
            "status" -> "ok",
            "anomalies" -> anomalies.map(a => {
              Json.obj(
                "severity" -> a.severity.toString,
                "desc" -> a.toShortString,
                "links" -> a.relatedLinks.map(rl => Json.obj(
                  "name" -> rl._1,
                  "link" -> rl._2.path()
                ))
              )
            })
          ))
        })
      case _ =>
        Future.successful(Ok(Json.obj(
          "status" -> "error",
          "message" -> "No such anomaly detector"
        )))
    }

  }
}