package controllers.admin

import auth.Authentication
import com.google.inject.Inject
import dao.ResponsesDAO
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by wyozi on 4.2.2016.
  */
class Responses @Inject()
  (val cc: ControllerComponents,
   auth: Authentication,
   controllerComponents: ControllerComponents, listTemplate: views.html.admin.resps, viewTemplate: views.html.admin.resp_view,
   responsesDAO: ResponsesDAO)
  (implicit parser: BodyParsers.Default) extends AbstractController(cc) with I18nSupport {

  import play.api.data.Forms._
  val responseForm = Form(
    tuple(
      "name" -> text,
      "response" -> text
    )
  )

  def list: Action[AnyContent] = auth.adminOnlyAsync { implicit request =>
    responsesDAO.getAll().map(resps => Ok(listTemplate(resps, responseForm)))
  }

  def view(respId: Int): Action[AnyContent] = auth.adminOnlyAsync { implicit request =>
    responsesDAO.findById(respId).map {
      case Some(resp) =>
        Ok(viewTemplate(resp))
    }
  }

  def editName(respId: Int): Action[AnyContent] = auth.adminOnlyAsync { implicit request =>
    val form = Form("name" -> text)

    form.bindFromRequest.fold(
      errors => Future.successful(BadRequest("invalid form")),
      name => responsesDAO.updateName(respId, name).map { x =>
        Ok("")
      }
    )
  }

  def editBody(respId: Int): Action[AnyContent] = auth.adminOnlyAsync { implicit request =>
    val form = Form("body" -> text)

    form.bindFromRequest.fold(
      errors => Future.successful(BadRequest("invalid form")),
      body => responsesDAO.updateBody(respId, body).map { x =>
        Ok("")
      }
    )
  }

  def create: Action[AnyContent] = auth.adminOnly { implicit request =>
    responseForm.bindFromRequest().fold(
      formWithErrors => BadRequest(listTemplate(Seq(), formWithErrors)),
      resp => {
        responsesDAO.insert(resp._1, resp._2)
        Redirect(routes.Responses.list())
      }
    )
  }
}
