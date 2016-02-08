package dao

import java.sql.Date

import com.google.inject.Inject
import model.{Ping, Product}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by wyozi on 8.2.2016.
  */
class PingsDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  private val Pings = TableQuery[PingsTable]

  def findRecentForProduct(prod: Product): Future[Seq[Ping]] =
    db.run(Pings.filter(_.product === prod.shortName).sortBy(_.id.desc).result)


  private class PingsTable(tag: Tag) extends Table[Ping](tag, "PINGS") {
    /*

  id          SERIAL UNIQUE,

  product     VARCHAR(255) NOT NULL REFERENCES Products (short_name),
  license     VARCHAR(255) NOT NULL,
  user_name   VARCHAR(64)  NOT NULL,
  date        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  response_id INT       DEFAULT NULL REFERENCES Responses (id),
     */
    def id = column[Int]("ID", O.AutoInc)

    def product = column[String]("PRODUCT", O.SqlType("VARCHAR(255)"))
    def license = column[String]("LICENSE", O.SqlType("VARCHAR(255)"))
    def userName = column[String]("USER_NAME", O.SqlType("VARCHAR(64)"))
    def ip = column[String]("IP", O.SqlType("VARCHAR(16)"))
    def date = column[Date]("DATE")

    def responseId = column[Option[Int]]("RESPONSE_ID")

    override def * = (id, date, product, license, userName, ip, responseId) <> (Ping.tupled, Ping.unapply _)

    //def response = foreignKey("RESPONSE_FK", responseId, )
  }
}
