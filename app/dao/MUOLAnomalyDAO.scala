package dao

import com.google.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import slick.jdbc.GetResult

import scala.concurrent.Future

/**
  * Created by wyozi on 8.2.2016.
  */
@Singleton
class MUOLAnomalyDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  case class MUOLAnomalyResult(productId: Int, productName: String, license: String, distinctUserCount: Int)
  implicit val getAnomalyResult = GetResult(r => MUOLAnomalyResult(r.<<, r.<<, r.<<, r.<<))

  def findDistinctUserGroups(threshold: Int): Future[Vector[MUOLAnomalyResult]] = {
    db.run(sql"""
        SELECT *
        FROM (
          SELECT products.id AS product_id, products.name AS product_name, license, COUNT(DISTINCT user_name) AS distinctUserCount FROM pings
            LEFT JOIN products ON pings.product = products.short_name
          GROUP BY product_id, license
        ) AS t
        WHERE distinctUserCount >= ${threshold}
           """.as[MUOLAnomalyResult])
  }
}
