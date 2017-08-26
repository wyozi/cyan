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

class ProductUsers @Inject() ()
  (implicit ec: ExecutionContext,
    backend: Backend,
    pingsDAO: PingsDAO,
    productsDAO: ProductsDAO,
    responsesDAO: ResponsesDAO,
    pingResponsesDAO: PingResponsesDAO,
    pingExtrasDAO: PingExtrasDAO,
    parser: BodyParsers.Default,
    usersDAO: UsersDAO) extends Controller with Secured {

  def list(productId: Int, withinHours: Option[Long] = None) = SecureAction.async {
    val hours = withinHours.getOrElse(24L)
    val hoursAgo = Timestamp.from(Instant.now().minus(hours, ChronoUnit.HOURS))

    for {
      prod <- productsDAO.findById(productId).map(_.get)
      users <- usersDAO.findDistinctUsersOf(prod, hoursAgo)
    } yield Ok(views.html.admin.prod_user_list(prod, hours, users))
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