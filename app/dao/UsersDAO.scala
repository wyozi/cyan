package dao

import java.sql.Timestamp

import com.google.inject.{Inject, Singleton}
import model.Ping
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future

@Singleton
class UsersDAO  @Inject() (protected val dbConfigProvider: DatabaseConfigProvider, pingsDAO: PingsDAO)
    extends HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  /**
    *
    * @param prod
    * @return the latest ping for each user that has used this product
    */
  def findDistinctUsersOf(prod: model.Product, afterTimestamp: Option[Timestamp] = None): Future[Seq[Ping]] = {
    db.run(
      pingsDAO.Pings.filter(p => p.product === prod.shortName)
        .filter(afterTimestamp
          .map(ts => (p: pingsDAO.PingsTable) => p.date >= ts)
          .getOrElse((p: pingsDAO.PingsTable) => true:Rep[Boolean]))
        .distinctOn(_.userName)
        .sortBy(_.date.desc)
        .result
    )
  }
}
