package controllers.admin.anomalydetection

import play.api.db.DB

/**
  * Created by wyozi on 3.2.2016.
  */
object ManyUsersOneLicense extends AnomalyDetector {
  val USER_PER_LICENSE_THRESHOLD = 4

  override def name: String = "Many users on license"

  override def detectAnomalies(): List[Anomaly] = {
    import play.api.Play.current
    DB.withConnection { implicit connection =>
      import anorm._
      import anorm.SqlParser._

      // TODO test

      val dbEntries = SQL("""
          |SELECT product, licenseId, COUNT(DISTINCT userId) AS distinctUserCount FROM Pings
          |GROUP BY product, licenseId
        """.stripMargin)
        .as(get[String]("product")~get[String]("licenseId")~get[Int]("distinctUserCount")*)

      dbEntries.map { case p~l~uc => Anomaly(s"$uc users on license $l in $p", Medium) }
    }
  }

  class MUOLAnomaly(name: String, severity: AnomalySeverity) extends Anomaly(name, severity) {
    override def toShortString: String = s"$name [${severity.getClass.getSimpleName}]"
  }
}
