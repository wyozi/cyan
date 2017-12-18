package controllers.admin.prod

import java.sql.Timestamp
import java.time.Instant
import java.time.temporal.ChronoUnit

import auth.Secured
import com.google.inject.Inject
import cyan.backend.Backend
import dao.{ProductsDAO, _}
import model.Ping
import play.api.mvc.{BodyParsers, Controller}

import scala.concurrent.ExecutionContext

class ProductUsers @Inject()
  (val listTemplate: views.html.admin.prod_user_list,
   productsDAO: ProductsDAO,
   usersDAO: UsersDAO)
  (implicit ec: ExecutionContext, parser: BodyParsers.Default) extends Controller with Secured {

  def list(productId: Int, withinHours: Option[Long] = None) = SecureAction.async {
    val hours = withinHours.getOrElse(24L)
    val hoursAgo = Timestamp.from(Instant.now().minus(hours, ChronoUnit.HOURS))

    for {
      prod <- productsDAO.findById(productId).map(_.get)
      users <- usersDAO.findDistinctUsersOf(prod, hoursAgo)
    } yield Ok(listTemplate(prod, hours, users))
  }

  private def buildCSV(users: Seq[Ping]) = "User\n" + users.map(_.user).mkString("\n")

  def export(productId: Int, format: String) = SecureAction.async {
    val hours = 999999L
    val hoursAgo = Timestamp.from(Instant.now().minus(hours, ChronoUnit.HOURS))

    for {
      prod <- productsDAO.findById(productId).map(_.get)
      users <- usersDAO.findDistinctUsersOf(prod, hoursAgo)
    } yield Ok(buildCSV(users))
  }

}