package controllers.admin

import javax.inject.Inject

import auth.Secured
import controllers.routes
import model.Product
import play.api.data.Form
import play.api.db.DB
import play.api.mvc.Controller

import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import response.ResponseFinder

/**
  * Created by wyozi on 4.2.2016.
  */
class Products @Inject() (implicit responseFinder: ResponseFinder) extends Controller with Secured {
  import play.api.data.Forms._
  val productForm = Form(
    tuple(
      "name" -> text,
      "charId" -> text
    )
  )

  def list = SecureAction {
    Ok(views.html.admin_prods(productForm))
  }

  def view(prodId: Int) = SecureAction {
    val prod = Product.getId(prodId).get
    Ok(views.html.admin_prod_view(prod))
  }

  def configure(prodId: Int) = SecureAction { req =>
    val fue = req.body.asFormUrlEncoded

    val opt = fue.get("opt").head
    val prod = Product.getId(prodId).get

    opt match {
      case "unreg_response" => {
        prod.updateDefaultUnregResponse(fue.get("response").head match { case "null" => Option.empty; case x => Some(x.toInt) })
      }
      case "reg_response" => {
        prod.updateDefaultRegResponse(fue.get("response").head match { case "null" => Option.empty; case x => Some(x.toInt) })
      }
    }

    Redirect(routes.Products.view(prodId))
  }

  def create = SecureAction { implicit request =>
    productForm.bindFromRequest().fold(
      formWithErrors => BadRequest(views.html.admin_prods(formWithErrors)),
      prod => {
        import play.api.Play.current
        DB.withConnection { c =>
          Product.insert(prod._1, prod._2)

          Redirect(routes.Products.list())
        }
      }
    )
  }

  def licenseView(prodId: Int, licenseId: String) = SecureAction {
    val prod = Product.getId(prodId).get
    Ok(views.html.admin_license_view(prod.getLicense(licenseId)))
  }

  def setProductLicenseResponse(productId: Int) = SecureAction { req =>
    val params = req.body.asFormUrlEncoded.get

    val user = params.get("user").map(_.head.mkString).get
    val license = params.get("license").map(_.head.mkString).get
    val response = params.get("response").map(_.head.mkString).get

    val responseParsed = response match {
      case "null" => Option.empty
      case _ => Some(response.toInt)
    }

    import play.api.Play.current
    DB.withConnection { implicit connection =>
      import anorm._

      SQL("MERGE INTO PingResponses(userId, licenseId, productId, response) KEY(userId, licenseId, productId) VALUES({user}, {license}, {product}, {resp})")
        .on('user -> user)
        .on('license -> license)
        .on('product -> productId)
        .on('resp -> responseParsed)
        .executeUpdate()

      Redirect(routes.Products.licenseView(productId, license))
    }
  }
}
