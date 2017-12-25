package dao

import java.sql.Timestamp

import com.google.inject.{Inject, Singleton}
import model.Ping
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.{GetResult, JdbcProfile}

import scala.concurrent.Future

@Singleton
class UsersDAO  @Inject() (protected val dbConfigProvider: DatabaseConfigProvider, pingsDAO: PingsDAO)
    extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  implicit val getPingResult = GetResult(r => Ping(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))

  /**
    *
    * @param prod
    * @return the latest ping for each user that has used this product
    */
  def findDistinctUsersOf(prod: model.Product, afterTimestamp: Timestamp): Future[Seq[Ping]] = {
    db.run(
      pingsDAO.Pings
        .join (
          // Following query obtains latest ping by id for each user that pinged with this product after timestamp
          // TODO: this inner query can probably be removed when slick bug #1340 is fixed
          pingsDAO.Pings
            .filter(p => p.productId === prod.id && p.date >= afterTimestamp)
            .sortBy(r => (r.userName, r.date.desc))
            .groupBy(_.userName).map(_._2.map(_.id).max)
        )
        .on(_.id === _)
        .map(_._1)
        .sortBy(_.date.desc)
        .result
    )
  }
}
