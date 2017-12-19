package dao.anomalydet

import com.google.inject.{Inject, Singleton}
import dao.{ProductsDAO, PingsDAO}
import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

@Singleton
class MLOUAnomalyDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider, pingsDAO: PingsDAO, productsDAO: ProductsDAO)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def findUserLicenseCount(threshold: Int): Future[Seq[(model.Product, String, Int)]] = {
    db.run(
      pingsDAO.Pings
        .groupBy(pi => (pi.product, pi.userName))
        .map { case ((prod, userName), rows) => (prod, userName, rows.map(_.license).distinct.length) }
        .filter(r => r._3 >= threshold)
      join
        productsDAO.Products
      on (_._1 === _.shortName)
      map {
        case ((prodName, userName, count), prod) => (prod, userName, count)
      }
      result
    )
  }
}
