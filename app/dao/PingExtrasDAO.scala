package dao

import com.google.inject.{Singleton, Inject}
import model.PingExtra
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by wyozi on 8.2.2016.
  */
@Singleton
class PingExtrasDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider, pingsDAO: PingsDAO)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  private[dao] val PingExtras = TableQuery[PingExtrasTable]

  def insert(extra: PingExtra): Future[Unit] =
    db.run(PingExtras += extra).map(_ => ())

  def findProductExtraDistinctValueCounts(product: String, key: String): Future[Seq[(Option[String], Int)]] = {
    val q = for {
      (p, e) <- pingsDAO.Pings.filter(_.product === product) joinLeft PingExtras.filter(_.key === key) on (_.id === _.pingId)
    } yield (e.map(_.value))

    db.run(
      q.groupBy(x => x).map { case (value, values) => (value, values.length) }.result
    )
  }

  def findExtras(pingId: Int): Future[Seq[PingExtra]] =
    db.run(PingExtras.filter(_.pingId === pingId).result)

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
