package controllers.admin.prod

import java.sql.Timestamp
import java.time.Instant
import java.time.temporal.ChronoUnit

import auth.Authentication
import com.google.inject.Inject
import dao.{ProductsDAO, _}
import model.Ping
import play.api.mvc._

import scala.concurrent.ExecutionContext

class ProductUsers @Inject()
  (val cc: ControllerComponents,
   auth: Authentication,
   listTemplate: views.html.admin.prod_user_list,
   productsDAO: ProductsDAO,
   usersDAO: UsersDAO)
  (implicit ec: ExecutionContext, parser: BodyParsers.Default) extends AbstractController(cc) {

  def list(productId: Int, withinHours: Option[Long] = None): Action[AnyContent] = auth.adminOnlyAsync { _ =>
    val hours = withinHours.getOrElse(24L)
    val hoursAgo = Timestamp.from(Instant.now().minus(hours, ChronoUnit.HOURS))

    for {
      prod <- productsDAO.findById(productId).map(_.get)
      users <- usersDAO.findDistinctUsersOf(prod, hoursAgo)
    } yield Ok(listTemplate(prod, hours, users))
  }

  private def buildCSV(users: Seq[Ping]) = "User\n" + users.map(_.user).mkString("\n")

  def export(productId: Int, format: String): Action[AnyContent] = auth.adminOnlyAsync { _ =>
    val hours = 999999L
    val hoursAgo = Timestamp.from(Instant.now().minus(hours, ChronoUnit.HOURS))

    for {
      prod <- productsDAO.findById(productId).map(_.get)
      users <- usersDAO.findDistinctUsersOf(prod, hoursAgo)
    } yield Ok(buildCSV(users))
  }

}