package controllers.admin

import auth.Secured
import com.google.inject.Inject
import cyan.backend.Backend
import dao.{PingExtrasDAO, PingsDAO, ProductsDAO, ResponsesDAO}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{BaseController, BodyParsers, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by wyozi on 4.2.2016.
  */
class Responses @Inject()(val controllerComponents: ControllerComponents) (implicit backend: Backend, productsDAO: ProductsDAO, pingsDAO: PingsDAO, pingExtrasDAO: PingExtrasDAO, responsesDAO: ResponsesDAO,  parser: BodyParsers.Default) extends BaseController with Secured with I18nSupport {
  import play.api.data.Forms._
  val responseForm = Form(
    tuple(
      "name" -> text,
      "response" -> text
    )
  )

  def list = SecureAction.async { implicit request =>
    responsesDAO.getAll().map(resps => Ok(views.html.admin.resps(resps, responseForm)))
  }

  def view(respId: Int) = SecureAction.async { implicit request =>
    responsesDAO.findById(respId).map {
      case Some(resp) =>
        Ok(views.html.admin.resp_view(resp))
    }
  }

  def editName(respId: Int) = SecureAction.async { implicit request =>
    val form = Form("name" -> text)

    form.bindFromRequest.fold(
      errors => Future.successful(BadRequest("invalid form")),
      name => responsesDAO.updateName(respId, name).map { x =>
        Ok("")
      }
    )
  }

  def editBody(respId: Int) = SecureAction.async { implicit request =>
    val form = Form("body" -> text)

    form.bindFromRequest.fold(
      errors => Future.successful(BadRequest("invalid form")),
      body => responsesDAO.updateBody(respId, body).map { x =>
        Ok("")
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
