package controllers.admin

import auth.Authentication
import com.google.inject.Inject
import dao._
import play.api.data.Form
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class Search @Inject()
  (cc: ControllerComponents,
   auth: Authentication,
   layout_admin_simple: views.html.admin.layout_admin_simple)
  (implicit ec: ExecutionContext, pingsDAO: PingsDAO, parser: BodyParsers.Default, productsDAO: ProductsDAO) extends AbstractController(cc) {

  import play.twirl.api.StringInterpolation
  import play.api.data.Forms._
  val queryForm = Form("query" -> text)

  def search: Action[AnyContent] = auth.adminOnlyAsync { implicit request =>
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
            case _ => Ok(layout_admin_simple(Seq(html"""Search: <code>$query</code>"""))(html"No results!"))
          }
      }
    )
  }
}
