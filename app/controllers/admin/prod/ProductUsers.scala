package controllers.admin.prod

import auth.Secured
import com.google.inject.Inject
import cyan.backend.Backend
import dao.{ProductsDAO, _}
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

class ProductUsers @Inject() ()
  (implicit ec: ExecutionContext,
    backend: Backend,
    pingsDAO: PingsDAO,
    productsDAO: ProductsDAO,
    responsesDAO: ResponsesDAO,
    pingResponsesDAO: PingResponsesDAO,
    usersDAO: UsersDAO) extends Controller with Secured {

  def list(productId: Int) = SecureAction.async {
    for {
      prod <- productsDAO.findById(productId).map(_.get)
      users <- usersDAO.findDistinctUsersOf(prod)
    } yield Ok(views.html.admin.prod_user_list(prod, users))
    /*
    productsDAO.findById(productId).flatMap {
      case Some(prod) =>
        usersDAO.findDistinctUsersOf(prod).map(s => (prod, s))
    }.map { (prod, users) =>
      Ok(views.html.admin.prod_user_list(prod, users))
    }*/
  }
}
