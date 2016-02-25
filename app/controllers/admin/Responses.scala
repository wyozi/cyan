package controllers.admin

import auth.Secured
import com.google.inject.Inject
import dao.{PingExtrasDAO, PingsDAO, ProductsDAO, ResponsesDAO}
import play.api.Play.current
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by wyozi on 4.2.2016.
  */
class Responses @Inject() (implicit productsDAO: ProductsDAO, pingsDAO: PingsDAO, pingExtrasDAO: PingExtrasDAO, responsesDAO: ResponsesDAO) extends Controller with Secured {
  import play.api.data.Forms._
  val responseForm = Form(
    tuple(
      "name" -> text,
      "response" -> text
    )
  )

  def list = SecureAction.async {
    responsesDAO.getAll().map(resps => Ok(views.html.admin.resps(resps, responseForm)))
  }

  def view(respId: Int) = SecureAction.async {
    responsesDAO.findById(respId).map {
      case Some(resp) =>
        Ok(views.html.admin.resp_view(resp))
    }
  }

  def editBody(respId: Int) = SecureAction.async { implicit request =>
    val form = Form("body" -> text)

    form.bindFromRequest.fold(
      errors => Future.successful(BadRequest("invalid form")),
      body => responsesDAO.updateBody(respId, body).map { x =>
        Redirect(routes.Responses.view(respId))
      }
    )
  }

  def create = SecureAction { implicit request =>
    responseForm.bindFromRequest().fold(
      formWithErrors => BadRequest(views.html.admin.resps(Seq(), formWithErrors)),
      resp => {
        responsesDAO.insert(resp._1, resp._2)
        Redirect(routes.Responses.list())
      }
    )
  }
}
