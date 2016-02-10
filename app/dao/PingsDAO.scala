package dao

import java.sql.{Timestamp, Date}

import com.google.inject.Inject
import model.{Ping, Product}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
  * Created by wyozi on 8.2.2016.
  */
class PingsDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  private[dao] val Pings = TableQuery[PingsTable]

  def insert(product: String, license: String, user: String, remoteAddress: String, responseId: Option[Int]): Future[Int] =
    db.run((Pings.map(c => (c.product, c.license, c.userName, c.ip, c.responseId)) returning Pings.map(_.id)) += (product, license, user, remoteAddress, responseId))

  def findRecentForProduct(prod: Product): Future[Seq[Ping]] =
    db.run(Pings.filter(_.product === prod.shortName).sortBy(_.id.desc).result)


  private[dao] class PingsTable(tag: Tag) extends Table[Ping](tag, "PINGS") {
    def id = column[Int]("ID", O.AutoInc)

    def product = column[String]("PRODUCT", O.SqlType("VARCHAR(255)"))
    def license = column[String]("LICENSE", O.SqlType("VARCHAR(255)"))
    def userName = column[String]("USER_NAME", O.SqlType("VARCHAR(64)"))
    def date = column[Timestamp]("DATE")
    def responseId = column[Option[Int]]("RESPONSE_ID")

    def ip = column[String]("IP", O.SqlType("VARCHAR(16)"))

    override def * = (id, date, product, license, userName, ip, responseId) <> (Ping.tupled, Ping.unapply _)
    //def response = foreignKey("RESPONSE_FK", responseId, )
  }
}
