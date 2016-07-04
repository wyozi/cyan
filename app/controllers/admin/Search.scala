package controllers.admin

import auth.Secured
import com.google.inject.Inject
import dao._
import play.api.data.Form
import play.api.mvc.Controller

import scala.concurrent.{ExecutionContext, Future}

class Search @Inject() (implicit ec: ExecutionContext, pingsDAO: PingsDAO, productsDAO: ProductsDAO) extends Controller with Secured {
  import cyan.util.TwirlHelpers._
  import play.api.data.Forms._
  val queryForm = Form("query" -> text)

  def search = SecureAction.async { implicit request =>
    queryForm.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest("form errors: " + formWithErrors)), // TODO better error

      query => {

        val licenseSearch =
          pingsDAO
            .findRecentWithLicense(query, 1)
            .flatMap {
              case x if x.nonEmpty => {
                Future.successful(Seq(Redirect(controllers.admin.routes.GlobalLicenses.viewLicense(query))))
              }
              case _ => Future.successful(Seq())
            }

        val userSearch =
          pingsDAO
            .findRecentByUser(query, 1)
            .flatMap {
              case x if x.nonEmpty => {
                Future.successful(Seq(Redirect(controllers.admin.routes.Users.viewUser(x.head.user))))
              }
              case _ => Future.successful(Seq())
            }

        val ipSearch =
          pingsDAO
            .findRecentByIp(query, 1)
            .flatMap {
              case x if x.nonEmpty => {
                Future.successful(Seq(Redirect(controllers.admin.routes.IPs.viewIp(x.head.ip))))
              }
              case _ => Future.successful(Seq())
            }

        Future
          .sequence(licenseSearch :: userSearch :: ipSearch :: Nil)
          .map(_.flatten.head) // flatten results and pick first one
          .recover {
            case _ => Ok(views.html.admin.layout_admin_simple(Seq(html"""Search: <code>$query</code>"""))(html"No results!"))
          }
      }
    )
  }
}
