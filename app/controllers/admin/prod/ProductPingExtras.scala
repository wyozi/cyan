package controllers.admin.prod

import auth.Authentication
import com.google.inject.Inject
import dao._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by wyozi on 16.2.2016.
  */
class ProductPingExtras @Inject()
  (val cc: ControllerComponents,
   auth: Authentication,
   listTemplate: views.html.admin.prod_pingextra_list,
   viewKeyTemplate: views.html.admin.prod_pingextra_view,
   viewValueTemplate: views.html.admin.prod_pingextra_view_value,
   productsDAO: ProductsDAO)
  (implicit parser: BodyParsers.Default) extends AbstractController(cc) {

  def list(prodId: Int): Action[AnyContent] = auth.adminOnlyAsync { _ =>
    productsDAO.findById(prodId).map {
      case Some(prod) => Ok(listTemplate(prod))
    }
  }

  def view(prodId: Int, key: String, days: Int, value: Option[String]) = auth.adminOnlyAsync { implicit request =>
    productsDAO.findById(prodId).map {
      case Some(prod) => Ok(value match {
        case Some(v) => viewValueTemplate(prod, key, v)
        case _ => viewKeyTemplate(prod, key, days)
      })
    }
  }

}
