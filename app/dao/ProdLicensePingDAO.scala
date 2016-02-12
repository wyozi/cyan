package dao

import com.google.inject.Inject
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
class ProdLicensePingDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider,
  pingsDAO: PingsDAO)
  extends HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  implicit val getAnomalyResult = GetResult(r => Ping(id = r.<<, product = r.<<, license = r.<<, user = r.<<, date = r.<<, responseId = r.<<, ip = r.<<))

  /**
    * Returns a list of pings with license in product. Ordered by latest ping timestamp descendingly. One per user.
    */
  def findUserPings(prodLicense: ProductLicense): Future[Seq[Ping]] =
    db.run(
      (
        for {
          p <- pingsDAO.Pings.filter(p => p.product === prodLicense.prod.shortName && p.license === prodLicense.license)
          maxTimestamp <- pingsDAO.Pings.groupBy(_.userName).map { case (user, pings) => pings.map(_.date).max }
          if p.date === maxTimestamp
        } yield p
      ).sortBy(_.date.desc).result
    )

  /**
    * Returns a list of pings in product. Ordered by first (oldest) ping timestamp descendingly. One per license.
    */
  def findRecentNewLicenses(prod: Product, limit: Int): Future[Seq[Ping]] = {
    val q = (
      for {
        p <- pingsDAO.Pings
        maxTimestamp <- pingsDAO.Pings.filter(_.product === prod.shortName)
          .groupBy(_.license)
          .map { case(user, pings) => pings.map(_.date).min }
        if p.date === maxTimestamp
      } yield p
    ).take(limit).sortBy(_.date.desc)
    db.run(q.result)
  }
}
