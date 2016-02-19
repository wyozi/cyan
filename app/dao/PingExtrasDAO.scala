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

  private def timestamp2date = SimpleExpression.unary[Timestamp, String] { (date, qb) =>
    qb.sqlBuilder += "DATE( "
    qb.expr(date)
    qb.sqlBuilder += ")"
  }

  def findProductExtraDistinctValueCountsPerDay(product: String, key: String, since: LocalDate, ignoredLicense: Option[String]): Future[Seq[(Option[String], Seq[(LocalDate, Int)])]] = {
    db.run(
      (
          pingsDAO.Pings
            .filter(pi => pi.product === product && pi.date >= new Timestamp(since.toDate.getTime))
            .filterNot(pi => ignoredLicense.map(pi.license === _).getOrElse(false:Rep[Boolean]))
        joinLeft
          PingExtras
            .filter(_.key === key)
        on (_.id === _.pingId)
      )
        .groupBy { case(pi, ex) => (timestamp2date(pi.date), ex.map(_.value)) }
        .map { case ((date, value), rows) => (date, value, rows.map(_._1.userName).countDistinct) }
        .result
    )
      .map(_.groupBy(_._2)
      .mapValues(
        _.map { case (t, v, c) => (new LocalDate(t), c) }
          .sortBy(_._1.toDateTimeAtStartOfDay.getMillis))
          .toSeq
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
