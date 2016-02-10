package controllers.admin

import javax.inject.Inject

import auth.Secured
import cyan.backend.Backend
import dao._
import model.{PingResponse, Product, ProductLicense}
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
  plpDAP: ProdLicensePingDAO,
  pingExtrasDAO: PingExtrasDAO) extends Controller with Secured {
  import play.api.data.Forms._
  val productForm = Form(
    tuple(
      "name" -> text,
      "charId" -> text
    )
  )

  def list = SecureAction.async {
    productsDAO.getAll().map(prods => Ok(views.html.admin_prods(prods, productForm)))
  }

  def view(prodId: Int) = SecureAction.async {
    val futureProd = productsDAO.findById(prodId)
    for {
      prodOpt <- productsDAO.findById(prodId)
      recentPings <- pingsDAO.findRecentForProduct(prodOpt.get)
    } yield Ok(views.html.admin_prod_view(prodOpt.get, recentPings))
  }

  def configure(prodId: Int) = SecureAction.async { req =>
    val fue = req.body.asFormUrlEncoded

    val opt = fue.get("opt").head
    productsDAO.findById(prodId).flatMap {
      case Some(prod) =>
        val f = opt match {
          case "unreg_response" => {
            val response = fue.get("response").head match {
              case "null" => Option.empty
              case x => Some(x.toInt)
            }
            pingResponsesDAO.upsertExactPingResponse(Some(prodId), None, None, response)
          }
        }
        f.map { r =>
          println(r)
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

  def licenseView(prodId: Int, licenseId: String) = SecureAction.async {
    productsDAO.findById(prodId).map {
      case Some(prod) =>
        Ok(views.html.admin_license_view(ProductLicense(prod, licenseId)))
    }
  }

  def setProductLicenseResponse(productId: Int, license: String) = SecureAction { req =>
    val params = req.body.asFormUrlEncoded.get

    val response = params.get("response").map(_.head.mkString).get match {
      case "null" => Option.empty
      case x => Some(x.toInt)
    }

    pingResponsesDAO.upsertExactPingResponse(Some(productId), Some(license), None, response)
    Redirect(routes.Products.licenseView(productId, license))
  }
}
