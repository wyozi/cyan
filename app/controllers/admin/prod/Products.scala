package controllers.admin.prod

import javax.inject.Inject

import auth.Secured
import cyan.backend.Backend
import dao._
import model.ProductConfig
import play.api.Play.current
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by wyozi on 4.2.2016.
  */
class Products @Inject() (implicit backend: Backend,
  responsesDAO: ResponsesDAO,
  pingResponsesDAO: PingResponsesDAO,
  pingsDAO: PingsDAO,
  productsDAO: ProductsDAO,
  productConfigDAO: ProductConfigDAO,
  plpDAO: ProdLicensePingDAO,
  pingExtrasDAO: PingExtrasDAO) extends Controller with Secured {
  import play.api.data.Forms._
  val productForm = Form(
    tuple(
      "name" -> text,
      "shortName" -> text
    )
  )

  def list = SecureAction.async {
    productsDAO.getAll().map(prods => Ok(views.html.admin_prods(prods, productForm)))
  }

  def view(prodId: Int) = SecureAction.async {
    val futureProd = productsDAO.findById(prodId)

    import shapeless._

    for {
      prod <- productsDAO.findById(prodId).map(_.get)
      devLicense <- prod.queryDevLicense()
      recentNewLicenses :: recentPings :: HNil <- util.FutureUtils.hsequence(plpDAO.findRecentNewLicenses(prod, 15, devLicense) :: pingsDAO.findRecentForProduct(prod, 15, devLicense) :: HNil)
    } yield Ok(views.html.admin_prod_view(prod, devLicense, recentNewLicenses, recentPings))
  }

  def configure(prodId: Int, configKey: String) = SecureAction.async { req =>
    val fue = req.body.asFormUrlEncoded

    productsDAO.findById(prodId).flatMap {
      case Some(prod) =>
        val f = configKey match {
          case "unreg_response" => {
            val response = fue.get("response").head match {
              case "null" => Option.empty
              case x => Some(x.toInt)
            }
            pingResponsesDAO.upsertExactPingResponse(Some(prodId), None, None, response)
          }
          case "devlicense" => {
            productConfigDAO.upsertValue(prodId, configKey, fue.get(ProductConfig.Keys.DevLicense).head)
          }
        }

        f.map { r =>
          Redirect(routes.Products.view(prodId))
        }
    }
  }

  def create = SecureAction { implicit request =>
    productForm.bindFromRequest().fold(
      formWithErrors => BadRequest(views.html.admin_prods(Seq(), formWithErrors)),
      prod => {
        productsDAO.insert(prod._1, prod._2)
        Redirect(routes.Products.list())
      }
    )
  }
}
