package views.snippet

import cyan.backend.Backend
import dao.{PingExtrasDAO, ProductConfigDAO, ProductsDAO, ResponsesDAO}
import model.{Ping, PingExtra, Product, Response}
import play.api.mvc.Call

import scala.concurrent.ExecutionContext

/**
  * Contains utilities for use with ping_table view
  */
object PingTable {
  import util.FutureUtils._
  def fetch(pings: Seq[Ping], showExtras: Boolean, showResponse: Boolean)(implicit productsDAO: ProductsDAO, responsesDAO: ResponsesDAO = null, pingExtrasDAO: PingExtrasDAO = null, productConfigDAO: ProductConfigDAO = null, backend: Backend = null, ec: ExecutionContext): Seq[(Ping, Product, Seq[PingExtra], Option[Response])] = {

    val allProducts = productsDAO.findByIds(pings.map(_.productId)).await()
    val allExtras = if (showExtras) pingExtrasDAO.findExtras(pings.map(_.id)).await() else Nil
    val allResponses = if (showResponse) responsesDAO.findByIds(pings.flatMap(_.responseId)).await() else Nil

    pings.map { ping =>
      (
        ping,
        allProducts.find(_.id == ping.productId).get,
        allExtras.filter(_.pingId == ping.id),
        ping.responseId.flatMap(rid => allResponses.find(_.id == rid))
      )
    }
  }

  // Add withQueryString to Calls to allow addition of new query string params in reverse routing
  // Source: https://stackoverflow.com/a/36320319
  implicit class CallOps (c: Call) {
    import play.shaded.ahc.io.netty.handler.codec.http.{QueryStringDecoder, QueryStringEncoder}

    import scala.collection.JavaConverters._

    def withQueryString(query: (String,Seq[String])*): Call = {
      val decoded = new QueryStringDecoder(c.url)
      val newUrl = new QueryStringEncoder(decoded.path())
      val params = decoded.parameters().asScala.mapValues(_.asScala.toSeq).toSeq
      for {
        (key, values) <- params ++ query
        value <- values
      } newUrl.addParam(key, value)
      Call(c.method, newUrl.toString, c.fragment)
    }
  }
}
