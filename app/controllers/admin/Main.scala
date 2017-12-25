package controllers.admin

import auth.Authentication
import com.google.inject.Inject
import cyan.backend.Backend
import dao._
import play.api.mvc._
import play.api.routing.JavaScriptReverseRouter

import scala.concurrent.ExecutionContext

class Main @Inject()
  (cc: ControllerComponents,
   auth: Authentication,
   template: views.html.admin.main)
  (implicit backend: Backend, productsDAO: ProductsDAO, productConfigDAO: ProductConfigDAO, pingsDAO: PingsDAO, pingExtrasDAO: PingExtrasDAO, responsesDAO: ResponsesDAO, parser: BodyParsers.Default, ec: ExecutionContext) extends AbstractController(cc) {

  def index: Action[AnyContent] = auth.adminOnly { implicit request =>
    Ok(template())
  }

  def logout: Action[AnyContent] = auth.adminOnly { _ =>
    Redirect(controllers.routes.Application.index()).withNewSession
  }

  def javascriptRoutes: Action[AnyContent] = auth.adminOnly { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsAdminRoutes")(
        controllers.admin.routes.javascript.BackendController.view,
        controllers.admin.prod.routes.javascript.ProductUsers.list,
        controllers.admin.prod.routes.javascript.ProductPingExtras.view
      )
    ).as("text/javascript")
  }
}
