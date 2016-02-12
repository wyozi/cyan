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
  def findUserPings(prodLicense: ProductLicense): Future[Seq[Ping]] =
    db.run(
      sql"""
         SELECT p1.*
         FROM pings p1
           LEFT JOIN pings p2
               ON p1.user_name = p2.user_name AND p1.product = p2.product AND p1.license = p2.license AND p1.id < p2.id
         WHERE p2.id is NULL AND p1.product = ${prodLicense.prod.shortName} AND p1.license = ${prodLicense.license}
      """.as[Ping]
    )

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
