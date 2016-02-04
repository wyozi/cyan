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
          |SELECT product, COUNT(DISTINCT userId) AS distinctUserCount FROM Pings
          |GROUP BY product
        """.stripMargin)
        .as(get[String]("product")~get[Int]("distinctUserCount")*)

      dbEntries.map { case p~uc => Anomaly(s"Many users on license in $p", Medium) }
    }
  }
}
