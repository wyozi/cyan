package model

import anorm._
import play.api.db.DB

/**
  * Created by wyozi on 4.2.2016.
  */
case class ProductLicense(prod: Product, license: String) {
  /**
    * Returns one ping object per user of this product with this license.
    * The returned ping object is always the one with newest timestamp.
    */
  def getLatestPingPerUser: List[Ping] = {
    import play.api.Play.current
    import anorm._

    DB.withConnection { implicit c =>
      SQL(
        // Right table contains id > left.id
        // If right.id is NULL, left side is the highest id
        // In that case retain left side
        """
          |SELECT p1.*
          |FROM Pings p1
          |  LEFT JOIN Pings p2
          |      ON p1.userId = p2.userId AND p1.product = p2.product AND p1.licenseId = p2.licenseId AND p1.id < p2.id
          |WHERE p2.id is NULL AND p1.product = {shortName} AND p1.licenseId = {license}
        """.stripMargin
      )
        .on('shortName -> prod.shortName)
        .on('license -> license)
        .as(Ping.productParser(this.prod).*)
    }
  }
}
