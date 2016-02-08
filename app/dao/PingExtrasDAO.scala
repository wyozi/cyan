package dao

import com.google.inject.Inject
import model.PingExtra
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
  * Created by wyozi on 8.2.2016.
  */
class PingExtrasDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  private val PingExtras = TableQuery[PingExtrasTable]

  def findValue(pingId: Int, key: String): Future[Option[String]] =
    db.run(PingExtras.filter(r => r.pingId === pingId && r.key === key).map(_.value).result.headOption)

  private class PingExtrasTable(tag: Tag) extends Table[PingExtra](tag, "PINGEXTRAS") {
    def pingId = column[Int]("PING_ID")
    def key = column[String]("KEY", O.SqlType("VARCHAR(16)"))
    def value = column[String]("VALUE")

    override def * = (pingId, key, value) <> (PingExtra.tupled, PingExtra.unapply)

    //def response = foreignKey("RESPONSE_FK", responseId, )
  }
}
