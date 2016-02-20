package controllers.admin

import auth.Secured
import com.google.inject.Inject
import dao._
import play.api.mvc.Controller
import play.twirl.api.Html

import scala.concurrent.ExecutionContext

class Search @Inject() (implicit ec: ExecutionContext, plpDAO: ProdLicensePingDAO, productsDAO: ProductsDAO) extends Controller with Secured {
  def search = SecureAction.async { req =>
    val fue = req.body.asFormUrlEncoded
    val query = fue.get("query").map(_.trim).head

    plpDAO
      .findRecentPings(query, 1)
      .flatMap {
        case x if x.nonEmpty => {
          productsDAO.findByShortName(x.head.product)
            .map {
              case Some(prod) => Redirect(controllers.admin.prod.routes.ProductLicenses.licenseView(prod.id, x.head.license))
            }
        }
      }
      .recover {
        case _ => Ok(views.html.admin.layout_admin_simple(Seq("Search Results"))(Html("No results!")))
      }
  }
}
