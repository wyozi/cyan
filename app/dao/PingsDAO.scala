package dao

import java.sql.Timestamp

import com.google.inject.{Singleton, Inject}
import model.{Ping, Product}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
  * Created by wyozi on 8.2.2016.
  */
@Singleton
class PingsDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  private[dao] val Pings = TableQuery[PingsTable]

  def insert(product: String, license: String, user: String, remoteAddress: String, responseId: Option[Int]): Future[Int] =
    db.run((Pings.map(c => (c.product, c.license, c.userName, c.ip, c.responseId)) returning Pings.map(_.id)) += (product, license, user, remoteAddress, responseId))

  def findRecent(limit: Int = 15): Future[Seq[Ping]] =
    db.run(Pings.sortBy(_.id.desc).take(limit).result)

  def findRecentForProduct(prod: Product, limit: Int = 1000, ignoredLicense: Option[String] = None): Future[Seq[Ping]] =
    db.run(
      Pings
        .filter(_.product === prod.shortName)
        .filterNot(pi => ignoredLicense.map(pi.license === _).getOrElse(false:Rep[Boolean]))
        .sortBy(_.id.desc)
        .take(limit)
        .result
    )

  def findRecentByIp(ip: String, limit: Int = 1000): Future[Seq[Ping]] =
    db.run(Pings.filter(_.ip === ip).sortBy(_.id.desc).take(limit).result)

  def findRecentByUser(user: String, limit: Int = 1000): Future[Seq[Ping]] =
    db.run(Pings.filter(_.userName === user).sortBy(_.id.desc).take(limit).result)

  def findRecentForResponse(resp: Option[Int], limit: Int): Future[Seq[Ping]] =
    db.run(
      Pings
        .filter(_.responseId === resp)
        .sortBy(_.id.desc)
        .take(limit)
        .result
    )

  private[dao] class PingsTable(tag: Tag) extends Table[Ping](tag, "pings") {
    def id = column[Int]("id", O.AutoInc)

    def product = column[String]("product", O.SqlType("VARCHAR(255)"))
    def license = column[String]("license", O.SqlType("VARCHAR(255)"))
    def userName = column[String]("user_name", O.SqlType("VARCHAR(64)"))
    def date = column[Timestamp]("date")
    def responseId = column[Option[Int]]("response_id")

    def ip = column[String]("ip", O.SqlType("VARCHAR(16)"))

    override def * = (id, date, product, license, userName, ip, responseId) <> (Ping.tupled, Ping.unapply _)
    //def response = foreignKey("RESPONSE_FK", responseId, )
  }
}
