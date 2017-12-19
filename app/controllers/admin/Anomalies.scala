package controllers.admin

import anomalydetection.AnomalyDetector
import auth.Authentication
import com.google.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, BodyParsers, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

class Anomalies @Inject()
  (val cc: ControllerComponents,
   auth: Authentication,
   detections: java.util.Set[AnomalyDetector], overviewView: views.html.admin.anomaly_overview)
  (implicit parser: BodyParsers.Default, ex: ExecutionContext) extends AbstractController(cc) {

  def overview = auth.adminOnly { req =>
    Ok(overviewView(detections))
  }

  def fetch(anomalyId: String) = auth.adminOnlyAsync { req =>
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