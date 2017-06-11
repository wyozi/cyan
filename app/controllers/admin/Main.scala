package controllers.admin

import auth.Secured
import com.google.inject.Inject
import cyan.backend.Backend
import dao._
import play.api.mvc.{Action, Controller}
import play.api.routing.JavaScriptReverseRouter

class Main @Inject() (implicit backend: Backend, productsDAO: ProductsDAO, productConfigDAO: ProductConfigDAO, pingsDAO: PingsDAO, pingExtrasDAO: PingExtrasDAO, responsesDAO: ResponsesDAO) extends Controller with Secured {
  def index = SecureAction {
    Ok(views.html.admin.main())
  }

  def javascriptRoutes = SecureAction { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsAdminRoutes")(
        controllers.admin.routes.javascript.BackendController.view,
        controllers.admin.prod.routes.javascript.ProductUsers.list
      )
    ).as("text/javascript")
  }
}
