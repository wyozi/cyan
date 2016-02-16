package dao

import java.sql.Timestamp

import com.google.inject.{Inject, Singleton}
import model.PingExtra
import org.joda.time.LocalDate
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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

  def findProductExtraDistinctValueCountsPerDay(product: String, key: String, since: LocalDate): Future[Seq[(Option[String], Seq[(LocalDate, Int)])]] = {
    db.run(
      sql"""
         SELECT pi."date"::date AS "date", pe."value" AS "value", COUNT(pe."value") AS count
         FROM "pings" pi
           LEFT JOIN "pingextras" pe
             ON pi.id = pe.ping_id AND pe.key = ${key}
         WHERE pi."product" = ${product} AND pi."date" >= ${new Timestamp(since.toDate.getTime)}
         GROUP BY pi."date"::date, pe."value"
         HAVING COUNT(pe."value") > 0
      """
        .as[(Timestamp, Option[String], Int)]
    ).map(_.groupBy(_._2).mapValues(_.map { case (t, v, c) => (new LocalDate(t), c) }.sortBy(_._1.toDateTimeAtStartOfDay.getMillis)).toSeq)
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
