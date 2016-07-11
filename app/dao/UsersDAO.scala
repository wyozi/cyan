package dao

import com.google.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future

@Singleton
class UsersDAO  @Inject() (protected val dbConfigProvider: DatabaseConfigProvider, pingsDAO: PingsDAO)
    extends HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  def findDistinctUsersOf(prod: model.Product): Future[Seq[String]] =
    db.run(
      pingsDAO.Pings.filter(p => p.product === prod.shortName)
        .map(_.userName)
        .distinct
        .result
    )
}
