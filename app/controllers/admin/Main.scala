package controllers.admin

import auth.Secured
import com.google.inject.Inject
import cyan.backend.Backend
import dao._
import play.api.mvc.{BodyParsers, Controller}
import play.api.routing.JavaScriptReverseRouter

import scala.concurrent.ExecutionContext

class Main @Inject() (template: views.html.admin.main)(implicit backend: Backend, productsDAO: ProductsDAO, productConfigDAO: ProductConfigDAO, pingsDAO: PingsDAO, pingExtrasDAO: PingExtrasDAO, responsesDAO: ResponsesDAO, parser: BodyParsers.Default, ec: ExecutionContext) extends Controller with Secured {

  def index = SecureAction { implicit request =>
    Ok(template())
  }

  def javascriptRoutes = SecureAction { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsAdminRoutes")(
        controllers.admin.routes.javascript.BackendController.view,
        controllers.admin.prod.routes.javascript.ProductUsers.list,
        controllers.admin.prod.routes.javascript.ProductPingExtras.view
      )
    ).as("text/javascript")
  }
}
