package controllers.admin.prod

import javax.inject.Inject

import auth.Secured
import cyan.backend.Backend
import dao._
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{BaseController, BodyParsers, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by wyozi on 4.2.2016.
  */
class Products @Inject()(val controllerComponents: ControllerComponents) (implicit backend: Backend,
  responsesDAO: ResponsesDAO,
  pingResponsesDAO: PingResponsesDAO,
  pingsDAO: PingsDAO,
  productsDAO: ProductsDAO,
  productConfigDAO: ProductConfigDAO,
  plpDAO: ProdLicensePingDAO,
  parser: BodyParsers.Default,
  pingExtrasDAO: PingExtrasDAO) extends BaseController with Secured with I18nSupport {
  import play.api.data.Forms._
  val productForm = Form(
    tuple(
      "name" -> text,
      "shortName" -> text
    )
  )

  def list = SecureAction.async { implicit request =>
    productsDAO.getAll().map(prods => Ok(views.html.admin.prods(prods, productForm)))
  }

  def view(prodId: Int, page: Int) = SecureAction.async { implicit request =>
    val futureProd = productsDAO.findById(prodId)

    for {
      prod <- productsDAO.findById(prodId).map(_.get)
      devLicense <- prod.queryDevLicense()
      recentPings <- pingsDAO.findRecentForProduct(prod, Products.PingsPerPage, offset = Products.PingsPerPage * page, ignoredLicense = devLicense)
    } yield Ok(views.html.admin.prod_view(prod, page, devLicense, recentPings))
  }


  import play.api.data.Forms._
  val productResponseForm = Form("response" -> optional(number))
  val productConfigForm = Form("value" -> text)

  def configure(prodId: Int, configKey: String) = SecureAction.async { implicit request =>
    productsDAO.findById(prodId).flatMap {
      case Some(prod) =>
        val f = configKey match {
          case "unreg_response" => {
            productResponseForm.bindFromRequest().fold(
              formWithErrors => Future.failed(new Exception("form error")),
              response => {
                pingResponsesDAO.upsertExactPingResponse(Some(prodId), None, None, response)
              }
            )
          }

          // assume it's a product config
          case key => {
            productConfigForm.bindFromRequest().fold(
              formWithErrors => Future.failed(new Exception("form error")),
              value => {
                productConfigDAO.upsertValue(prodId, configKey, value)
              }
            )
          }
        }

        f.map { r =>
          Redirect(routes.Products.view(prodId))
        }
    }
  }

  def create = SecureAction { implicit request =>
    productForm.bindFromRequest().fold(
      formWithErrors => BadRequest(views.html.admin.prods(Seq(), formWithErrors)),
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