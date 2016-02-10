package controllers.admin

import auth.Secured
import com.google.inject.Inject
import dao.{ProductsDAO, ResponsesDAO}
import model.Response
import play.api.data.Form
import play.api.mvc.Controller

import play.api.Play.current
import play.api.i18n.Messages.Implicits._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by wyozi on 4.2.2016.
  */
class Responses @Inject() (implicit productsDAO: ProductsDAO, responsesDAO: ResponsesDAO) extends Controller with Secured {
  import play.api.data.Forms._
  val responseForm = Form(
    tuple(
      "name" -> text,
      "response" -> text
    )
  )

  def list = SecureAction.async {
    responsesDAO.getAll().map(resps => Ok(views.html.admin_resps(resps, responseForm)))
  }

  def view(respId: Int) = SecureAction.async {
    responsesDAO.findById(respId).map {
      case Some(resp) =>
        Ok(views.html.admin_resp_view(resp))
    }
  }

  def create = SecureAction { implicit request =>
    responseForm.bindFromRequest().fold(
      formWithErrors => BadRequest(views.html.admin_resps(Seq(), formWithErrors)),
      resp => {
        responsesDAO.insert(resp._1, resp._2)
        Redirect(routes.Responses.list())
      }
    )
  }
}
