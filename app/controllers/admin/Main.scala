package controllers.admin

import auth.Secured
import com.google.inject.Inject
import dao.{ResponsesDAO, PingExtrasDAO, ProductsDAO, PingsDAO}
import play.api.mvc.{Action, Controller}
import play.api.routing.JavaScriptReverseRouter

class Main @Inject() (implicit productsDAO: ProductsDAO, pingsDAO: PingsDAO, pingExtrasDAO: PingExtrasDAO, responsesDAO: ResponsesDAO) extends Controller with Secured {
  def index = SecureAction {
    Ok(views.html.admin.main())
  }

  def javascriptRoutes = SecureAction { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsAdminRoutes")(
        controllers.admin.routes.javascript.Pings.showPingExtra
      )
    ).as("text/javascript")
  }
}
