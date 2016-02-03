package controllers

import auth.Secured
import controllers.Application._
import controllers.Ping._
import model.{Response, Product}
import play.api.data.Form
import play.api.db.DB
import play.api.mvc.{Action, Controller}

import play.api.Play.current
import play.api.i18n.Messages.Implicits._

object Admin extends Controller with Secured {
  import play.api.data.Forms._
  val productForm = Form(
    tuple(
      "name" -> text,
      "charId" -> text
    )
  )

  def productList = SecureAction {
    Ok(views.html.admin_prods(productForm))
  }

  def createProduct = SecureAction { implicit request =>
    productForm.bindFromRequest().fold(
      formWithErrors => BadRequest(views.html.admin_prods(formWithErrors)),
      prod => {
        import play.api.Play.current
        DB.withConnection { c =>
          Product.insert(prod._1, prod._2)

          Redirect(routes.Admin.productList())
        }
      }
    )
  }

  val responseForm = Form(
    tuple(
      "name" -> text,
      "response" -> text
    )
  )

  def responseList = SecureAction {
    Ok(views.html.admin_resps(responseForm))
  }

  def createResponse = SecureAction { implicit request =>
    responseForm.bindFromRequest().fold(
      formWithErrors => BadRequest(views.html.admin_resps(formWithErrors)),
      prod => {
        import play.api.Play.current
        DB.withConnection { c =>
          val s = c.prepareStatement("INSERT INTO Responses(name, response) VALUES (?, ?)")
          s.setString(1, prod._1)
          s.setString(2, prod._2)
          s.execute()

          Redirect(routes.Admin.responseList())
        }
      }
    )
  }

  def responseView(respId: Int) = SecureAction {
    val resp = Response.getId(respId).get
    Ok(views.html.admin_resp_view(resp))
  }

  def productView(prodId: Int) = SecureAction {
    val prod = Product.getId(prodId).get
    Ok(views.html.admin_prod_view(prod))
  }

  def licenseView(prodId: Int, licenseId: String) = SecureAction {
    val prod = Product.getId(prodId).get
    Ok(views.html.admin_license_view(prod, licenseId))
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

      Redirect(routes.Admin.licenseView(productId, license))
    }
  }

}