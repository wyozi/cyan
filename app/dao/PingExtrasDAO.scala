package dao

import com.google.inject.Inject
import model.PingExtra
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by wyozi on 8.2.2016.
  */
class PingExtrasDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] {


  import driver.api._

  private[dao] val PingExtras = TableQuery[PingExtrasTable]

  def insert(extra: PingExtra): Future[Unit] =
    db.run(PingExtras += extra).map(_ => ())

  def findExtras(id: Int): Future[Seq[PingExtra]] =
    db.run(PingExtras.filter(_.pingId === id).result)

  def findValue(pingId: Int, key: String): Future[Option[String]] =
    db.run(PingExtras.filter(r => r.pingId === pingId && r.key === key).map(_.value).result.headOption)

  private[dao] class PingExtrasTable(tag: Tag) extends Table[PingExtra](tag, "pingextras") {
    def pingId = column[Int]("ping_id")
    def key = column[String]("key", O.SqlType("VARCHAR(16)"))
    def value = column[String]("value")

    override def * = (pingId, key, value) <> (PingExtra.tupled, PingExtra.unapply)

    //def response = foreignKey("RESPONSE_FK", responseId, )
  }
}
