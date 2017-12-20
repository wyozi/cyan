package dao.anomalydet

import java.sql.Timestamp

import com.google.inject.{Inject, Singleton}
import dao.{PingsDAO, ProductsDAO}
import org.joda.time.LocalDateTime
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

/**
  * Created by wyozi on 8.2.2016.
  */
@Singleton
class MUOLAnomalyDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider, pingsDAO: PingsDAO, productsDAO: ProductsDAO)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def findDistinctUserGroups(threshold: Int, since: LocalDateTime): Future[Seq[(model.Product, String, Int)]] = {
    db.run(
        pingsDAO.Pings
          .filter(_.date >= new Timestamp(since.toDate.getTime))
          .groupBy(pi => (pi.productId, pi.license))
          .map { case ((prod, license), rows) => (prod, license, rows.map(_.userName).countDistinct) }
          .filter(r => r._3 >= threshold)
      join
        productsDAO.Products
      on (_._1 === _.id)
      map {
        case ((prodName, license, count), prod) => (prod, license, count)
      }
      result
    )
  }
}
