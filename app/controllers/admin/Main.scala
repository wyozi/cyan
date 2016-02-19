package controllers.admin

import auth.Secured
import com.google.inject.Inject
import dao.{ResponsesDAO, PingExtrasDAO, ProductsDAO, PingsDAO}
import play.api.mvc.Controller

class Main @Inject() (implicit productsDAO: ProductsDAO, pingsDAO: PingsDAO, pingExtrasDAO: PingExtrasDAO, responsesDAO: ResponsesDAO) extends Controller with Secured {
  def index = SecureAction {
    Ok(views.html.admin.main())
  }
}
