package dao

import java.sql.Timestamp

import com.google.inject.{Inject, Singleton}
import model.{Ping, Product, ProductLicense}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by wyozi on 8.2.2016.
  */
@Singleton
class PingsDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit val ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private[dao] val Pings = TableQuery[PingsTable]

  private def estimatedCount(sql: String): Future[Int] =
    db.run(
      sql"""
            SELECT count_estimate('#${sql}');
      """.as[Int]
    ).map(_.headOption.getOrElse(0))

  /**
    * Inserts given ping to Pings table and returns id.
    */
  def insert(productId:Int , license: String, user: String, remoteAddress: String, responseId: Option[Int]): Future[Int] =
    db.run((Pings.map(c => (c.productId, c.license, c.userName, c.ip, c.responseId)) returning Pings.map(_.id)) += (productId, license, user, remoteAddress, responseId))

  /**
    * Find latest pings sorted by ID descendingly.
    */
  def findRecent(amount: Int, offset: Int = 0): Future[Seq[Ping]] =
    db.run(Pings.sortBy(_.id.desc).drop(offset).take(amount).result)

  /**
    * Finds estimated number of pings in the database
    */
  def findEstimatedCount(): Future[Int] =
    estimatedCount(Pings.map(r => 1:Rep[Int]).result.statements.head)

  /**
    * Find recent pings for given product.
    *
    * @param prod
    * @param limit
    * @param ignoredLicense license to ignore from output
    */
  def findRecentForProduct(prod: Product, limit: Int, offset: Int = 0, ignoredLicense: Option[String] = None): Future[Seq[Ping]] =
    db.run(
      Pings
        .filter(_.productId === prod.id)
        .filterNot(pi => ignoredLicense.map(pi.license === _).getOrElse(false:Rep[Boolean]))
        .sortBy(_.id.desc)
        .drop(offset)
        .take(limit)
        .result
    )

  /**
    * Find recent pings by given ip.
    */
  def findRecentByIp(ip: String, limit: Int): Future[Seq[Ping]] =
    db.run(Pings.filter(_.ip === ip).sortBy(_.id.desc).take(limit).result)

  /**
    * Find recent pings by given user.
    */
  def findRecentByUser(user: String, limit: Int): Future[Seq[Ping]] =
    db.run(Pings.filter(_.userName === user).sortBy(_.id.desc).take(limit).result)

  /**
    * Find recent pings with given response id.
    */
  def findRecentWithResponse(resp: Option[Int], limit: Int, offset: Int = 0): Future[Seq[Ping]] =
    db.run(Pings.filter(_.responseId === resp).sortBy(_.id.desc).drop(offset).take(limit).result)

  /**
    * Finds estimated number of pings with given response
    */
  def findEstimatedCountWithResponse(resp: Option[Int]): Future[Int] =
    estimatedCount(Pings.filter(_.responseId === resp).map(r => 1:Rep[Int]).result.statements.head)

  /**
    * Find count of distinct licenses for given product.
    * Uses an estimation function for a faster estimated result
    */
  def findEstimatedLicenseCount(prod: Product): Future[Int] =
    estimatedCount(Pings.filter(_.productId === prod.id).map(_.license).distinct.result.statements.head)

  /**
    * Find amount of pings for given product.
    * Uses an estimation function for a faster estimated result
    */
  def findEstimatedPingCount(prod: Product): Future[Int] =
    estimatedCount(Pings.filter(_.productId === prod.id).map(r => 1:Rep[Int]).result.statements.head)

  /**
    * Find recent pings with given license.
    * NOTE: Queries for given license in '''all''' pings no matter the product. If you want to filter by product as well,
    * use findRecentWithProdLicense.
    */
  def findRecentWithLicense(license: model.License, limit: Int): Future[Seq[Ping]] =
    db.run(Pings.filter(_.license === license).sortBy(_.date.desc).take(limit).result)

  /**
    * Find recent pings with given product and license.
    */
  def findRecentWithProdLicense(prodLicense: ProductLicense, limit: Int): Future[Seq[Ping]] =
    db.run(
      Pings.filter(p => p.productId === prodLicense.prod.id && p.license === prodLicense.license)
        .sortBy(_.date.desc).take(limit).result
    )

  /**
    * Find recent pings with given pingextra key->value, optionally input the product
    */
  def findRecentWithExtraValue(pingExtraKey: String, pingExtraValue: String, amount: Int, product: Option[Product], offset: Int = 0)(implicit pingExtrasDAO: PingExtrasDAO): Future[Seq[Ping]] = {
    db.run(
      (
          Pings
            .filter(pi => product.map(_.id).map(pi.productId === _).getOrElse(true:Rep[Boolean]))
        join
          pingExtrasDAO.PingExtras
            .filter(pe => pe.key === pingExtraKey && pe.value === pingExtraValue)
        on ((a, b) => a.id === b.pingId)
      )
          .sortBy(_._1.id.desc)
          .drop(offset)
          .take(amount)
          .map { case (ping, extra) => ping }
          .result
    )
  }

  /**
    * Find estimated count of pings with given pingextra key->value, optionally input the product
    */
  def findEstimatedCountWithExtraValue(pingExtraKey: String, pingExtraValue: String, product: Option[Product])(implicit pingExtrasDAO: PingExtrasDAO): Future[Int] =
    estimatedCount(
      (
        Pings
          .filter(pi => product.map(_.id).map(pi.productId === _).getOrElse(true:Rep[Boolean]))
          join
          pingExtrasDAO.PingExtras
            .filter(pe => pe.key === pingExtraKey && pe.value === pingExtraValue)
          on ((a, b) => a.id === b.pingId)
      )
        .map(r => 1:Rep[Int])
        .result.statements.head
    )

  private[dao] class PingsTable(tag: Tag) extends Table[Ping](tag, "pings") {
    def id = column[Int]("id", O.AutoInc)

    def license = column[String]("license", O.SqlType("VARCHAR(255)"))
    def userName = column[String]("user_name", O.SqlType("VARCHAR(64)"))
    def date = column[Timestamp]("date")
    def responseId = column[Option[Int]]("response_id")
    def productId = column[Int]("product_id")

    def ip = column[String]("ip", O.SqlType("VARCHAR(16)"))

    override def * = (id, date, productId, license, userName, ip, responseId) <> (Ping.tupled, Ping.unapply _)
    //def response = foreignKey("RESPONSE_FK", responseId, )
  }
}
