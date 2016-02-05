package controllers.admin.anomalydetection

import play.api.db.DB
import play.api.mvc.Call

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

      val dbEntries = SQL("""
          |SELECT *
          |FROM (
          |  SELECT Products.id AS productId, Products.name AS productName, licenseId, COUNT(DISTINCT userId) AS distinctUserCount FROM Pings
          |    LEFT JOIN Products ON Pings.product = Products.shortName
          |  GROUP BY productId, licenseId
          |) AS t
          |WHERE distinctUserCount >= {threshold}
        """.stripMargin)
        .on('threshold -> USER_PER_LICENSE_THRESHOLD)
        .as(get[String]("productName")~get[Int]("productId")~get[String]("licenseId")~get[Int]("distinctUserCount")*)

      dbEntries.map { case pnm~p~l~uc => new MUOLAnomaly(pnm, p, l, uc) }
    }
  }

  class MUOLAnomaly(productName: String, productId: Int, license: String, distinctUserCount: Int) extends Anomaly(Medium) {
    override def relatedLinks: List[(String, Call)] = List(
      ("product", controllers.admin.routes.Products.view(productId)),
      ("license", controllers.admin.routes.Products.licenseView(productId, license))
    )

    override def toShortString: String = s"$distinctUserCount distinct users on a single license of $productName"
  }
}
