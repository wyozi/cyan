package controllers.admin.prod

import auth.Authentication
import com.google.inject.Inject
import dao._
import play.api.data.Form
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class ProductResponses @Inject()
  (val cc: ControllerComponents,
   auth: Authentication,
   listTemplate: views.html.admin.prod_resp_list,
   productsDAO: ProductsDAO,
   pingResponsesDAO: PingResponsesDAO)
  (implicit ec: ExecutionContext, parser: BodyParsers.Default) extends AbstractController(cc) {

  def list(prodId: Int): Action[AnyContent] = auth.adminOnlyAsync { _ =>
    productsDAO.findById(prodId).map {
      case Some(prod) =>
        Ok(listTemplate(prod))
    }
  }

  import play.api.data.Forms._
  val responseForm = Form(
    tuple(
      "license" -> text,
      "user" -> text,
      "response" -> optional(number)
    )
  )

  def insert(prodId: Int): Action[AnyContent] = auth.adminOnlyAsync { implicit request =>
    responseForm.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest("Error: " + formWithErrors.toString)), // TODO nicer error display
      pingResponse => {
        val licenseOpt = pingResponse._1 match { case "" => None; case x => Some(x) }
        val userOpt = pingResponse._2 match { case "" => None; case x => Some(x) }
        val respOpt = pingResponse._3

        pingResponsesDAO
          .upsertExactPingResponse(Some(prodId), licenseOpt, userOpt, respOpt)
          .map { x =>
            Redirect(routes.ProductResponses.list(prodId))
          }
      }
    )
  }
}

