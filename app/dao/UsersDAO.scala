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
    val s =
      sql"""
           select *
           from (select distinct on ("user_name") "id", "date", "product", "license", "user_name", "ip", "response_id" from "pings" where ("product" = ${prod.shortName}) and ("date" >= ${afterTimestamp}) order by "user_name", "date" desc) t
           order by "date" desc
         """
    db.run(
      s.as[Ping]
    )
  }
}
