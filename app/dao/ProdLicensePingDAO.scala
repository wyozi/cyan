package dao

import com.google.inject.{Singleton, Inject}
import model.{Ping, Product, ProductLicense}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import slick.jdbc.GetResult

import scala.concurrent.Future

/**
  * DAO for database operations related to products, licenses and pings (and their relations)
  *
  * Created by wyozi on 8.2.2016.
  */
@Singleton
class ProdLicensePingDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider,
  productConfigDAO: ProductConfigDAO,
  pingsDAO: PingsDAO)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  implicit val getAnomalyResult = GetResult(r => Ping(id = r.<<, product = r.<<, license = r.<<, user = r.<<, date = r.<<, responseId = r.<<, ip = r.<<))

  def findLicenseCount(prod: Product): Future[Int] =
    db.run(pingsDAO.Pings.filter(_.product === prod.shortName).map(_.license).countDistinct.result)

  def findPingCount(prod: Product): Future[Int] =
    db.run(pingsDAO.Pings.filter(_.product === prod.shortName).length.result)

  def findRecentPings(prod: Product, limit: Int): Future[Seq[Ping]] =
    db.run(pingsDAO.Pings.filter(_.product === prod.shortName).sortBy(_.date.desc).take(limit).result)

  def findRecentPings(license: model.License, limit: Int): Future[Seq[Ping]] =
    db.run(
      pingsDAO.Pings.filter(_.license === license)
      sortBy(_.date.desc)
      take(limit)
      result
    )

  def findRecentPings(prodLicense: ProductLicense, limit: Int): Future[Seq[Ping]] =
    db.run(
      pingsDAO.Pings
        .filter(p => p.product === prodLicense.prod.shortName && p.license === prodLicense.license)
        sortBy(_.date.desc)
        take(limit)
        result
    )

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
    * Returns a list of pings in product. Ordered by first (oldest) ping timestamp descendingly. One per license.
    */
  def findRecentNewLicenses(prod: Product, limit: Int, ignoredLicense: Option[String]): Future[Seq[Ping]] = {
    db.run(
        pingsDAO.Pings
          .filter(p => p.product === prod.shortName)
          .filterNot(pi => ignoredLicense.map(pi.license === _).getOrElse(false:Rep[Boolean]))
          .groupBy(_.license)
          .map { case (license, pings) => pings.map(_.id).min }
      join
        pingsDAO.Pings
      on (_ === _.id)
      map { case (id, ping) => ping }
      sortBy(_.date.desc)
      take(limit)
      result
    )
  }
}
