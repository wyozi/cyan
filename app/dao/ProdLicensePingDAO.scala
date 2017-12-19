package dao

import com.google.inject.{Inject, Singleton}
import model.{Ping, Product, ProductLicense}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.{GetResult, JdbcProfile}

import scala.concurrent.{ExecutionContext, Future}

/**
  * DAO for database operations related to products, licenses and pings (and their relations).
  * A rule of thumb for including a method here instead of some table-specific DAO is that the query requires access
  * to multiple tables (through joins etc)
  *
  * Created by wyozi on 8.2.2016.
  */
@Singleton
class ProdLicensePingDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider,
  productConfigDAO: ProductConfigDAO,
  productsDAO: ProductsDAO,
  pingsDAO: PingsDAO)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  implicit val getAnomalyResult = GetResult(r => Ping(id = r.<<, product = r.<<, license = r.<<, user = r.<<, date = r.<<, responseId = r.<<, ip = r.<<))

  def findProductLicensesByUser(user: String): Future[Seq[ProductLicense]] =
    db.run(
      (
        pingsDAO.Pings
          .filter(_.userName === user)
          .groupBy(r => (r.product, r.license))
          .map { case ((p, l), _) => (p, l) }
        join
          productsDAO.Products
        on (_._1 === _.shortName)
      )
        .map { case ((p, l), prod) => (prod, l) }
        .result
    ) map(_.map(r => ProductLicense(r._1, r._2)))

  /**
    * Returns a list of pings with license in product. Ordered by latest ping timestamp descendingly. One per user.
    */
  def findRecentUserPings(prodLicense: ProductLicense): Future[Seq[Ping]] =
    db.run(
        pingsDAO.Pings
          .filter(p => p.product === prodLicense.prod.shortName && p.license === prodLicense.license)
          .groupBy(_.userName)
          .map { case (user, pings) => pings.map(_.id).max }
      join
        pingsDAO.Pings
      on (_ === _.id)
      map { case (id, ping) => ping }
      sortBy(_.date.desc)
      result
    )

  /**
    * Returns one (the most recent) ping per product with given license.
    */
  def findMostRecentPingsWithLicense(license: model.License): Future[Seq[Ping]] = {
    db.run(
        pingsDAO.Pings
          .filter(p => p.license === license)
          .groupBy(_.product)
          .map { case (prod, pings) => pings.map(_.id).max }
      join
        pingsDAO.Pings
        on (_ === _.id)
      map { case (id, ping) => ping }
      result
    )
  }
}
