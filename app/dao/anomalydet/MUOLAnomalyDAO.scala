package dao.anomalydet

import com.google.inject.{Inject, Singleton}
import dao.{PingsDAO, ProductsDAO}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
  * Created by wyozi on 8.2.2016.
  */
@Singleton
class MUOLAnomalyDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider, pingsDAO: PingsDAO, productsDAO: ProductsDAO)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  def findDistinctUserGroups(threshold: Int): Future[Seq[(model.Product, String, Int)]] = {
    db.run(
        pingsDAO.Pings
          .groupBy(pi => (pi.product, pi.license))
          .map { case ((prod, license), rows) => (prod, license, rows.map(_.userName).countDistinct) }
          .filter(r => r._3 >= threshold)
      join
        productsDAO.Products
      on (_._1 === _.shortName)
      map {
        case ((prodName, license, count), prod) => (prod, license, count)
      }
      result
    )
  }
}
