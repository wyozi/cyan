package controllers.admin.prod

import javax.inject.Inject

import auth.Authentication
import dao._
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by wyozi on 4.2.2016.
  */
class Products @Inject()
  (val cc: ControllerComponents,
   auth: Authentication,
   controllerComponents: ControllerComponents,
   listTemplate: views.html.admin.prods,
   viewTemplate: views.html.admin.prod_view,
   productsDAO: ProductsDAO,
   pingResponsesDAO: PingResponsesDAO)
  (implicit parser: BodyParsers.Default, productConfigDAO: ProductConfigDAO) extends AbstractController(cc) with I18nSupport {

  import play.api.data.Forms._
  val productForm = Form(
    tuple(
      "name" -> text,
      "shortName" -> text
    )
  )

  def list: Action[AnyContent] = auth.adminOnlyAsync { implicit request =>
    productsDAO.getAll().map(prods => Ok(listTemplate(prods, productForm)))
  }

  def view(prodId: Int): Action[AnyContent] = auth.adminOnlyAsync { implicit request =>
    val futureProd = productsDAO.findById(prodId)

    for {
      prod <- productsDAO.findById(prodId).map(_.get)
      devLicense <- prod.queryDevLicense()
    } yield Ok(viewTemplate(prod, devLicense))
  }


  import play.api.data.Forms._
  val productResponseForm = Form("response" -> optional(number))
  val productConfigForm = Form("value" -> text)

  def configure(prodId: Int, configKey: String): Action[AnyContent] = auth.adminOnlyAsync { implicit request =>
    productsDAO.findById(prodId).flatMap {
      case Some(prod) =>
        val f = configKey match {
          case "unreg_response" =>
            productResponseForm.bindFromRequest().fold(
              formWithErrors => Future.failed(new Exception("form error")),
              response => {
                pingResponsesDAO.upsertExactPingResponse(Some(prodId), None, None, response)
              }
            )

          // assume it's a product config
          case key =>
            productConfigForm.bindFromRequest().fold(
              formWithErrors => Future.failed(new Exception("form error")),
              value => {
                productConfigDAO.upsertValue(prodId, configKey, value)
              }
            )
        }

        f.map { r =>
          Redirect(routes.Products.view(prodId))
        }
    }
  }

  def create: Action[AnyContent] = auth.adminOnly { implicit request =>
    productForm.bindFromRequest().fold(
      formWithErrors => BadRequest(listTemplate(Seq(), formWithErrors)),
      prod => {
        productsDAO.insert(prod._1, prod._2)
        Redirect(routes.Products.list())
      }
    )
  }
}
object Products {
  val PingsPerPage = 10
}