package controllers.admin

import auth.Secured
import model.Response
import play.api.data.Form
import play.api.db.DB
import play.api.mvc.Controller

import play.api.Play.current
import play.api.i18n.Messages.Implicits._

/**
  * Created by wyozi on 4.2.2016.
  */
class Responses extends Controller with Secured {
  import play.api.data.Forms._
  val responseForm = Form(
    tuple(
      "name" -> text,
      "response" -> text
    )
  )

  def list = SecureAction {
    Ok(views.html.admin_resps(responseForm))
  }

  def view(respId: Int) = SecureAction {
    val resp = Response.getById(respId).get
    Ok(views.html.admin_resp_view(resp))
  }

  def create = SecureAction { implicit request =>
    responseForm.bindFromRequest().fold(
      formWithErrors => BadRequest(views.html.admin_resps(formWithErrors)),
      prod => {
        import play.api.Play.current
        DB.withConnection { c =>
          val s = c.prepareStatement("INSERT INTO Responses(name, response) VALUES (?, ?)")
          s.setString(1, prod._1)
          s.setString(2, prod._2)
          s.execute()

          Redirect(routes.Responses.list())
        }
      }
    )
  }
}
