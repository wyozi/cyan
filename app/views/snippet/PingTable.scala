package views.snippet

import cyan.backend.Backend
import dao.{PingExtrasDAO, ProductConfigDAO, ProductsDAO, ResponsesDAO}
import model.{Ping, PingExtra, Product, Response}
import shapeless._

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * Contains utilities for use with ping_table view
  */
object PingTable {
  import util.FutureUtils._
  def fetch(pings: Seq[Ping], showExtras: Boolean, showResponse: Boolean)(implicit productsDAO: ProductsDAO, responsesDAO: ResponsesDAO = null, pingExtrasDAO: PingExtrasDAO = null, productConfigDAO: ProductConfigDAO = null, backend: Backend = null, ec: ExecutionContext): Seq[(Ping, Product, Seq[PingExtra], Option[Response])] = {

    val productCache = mutable.HashMap[String, Future[Product]]()
    val responseCache = mutable.HashMap[Option[Int], Future[Option[Response]]]()

    for(ping :: product :: extras :: response :: HNil <-
        pings.map(p => hsequence(
          Future.successful(p)
            :: productCache.getOrElseUpdate(p.product, p.queryProduct())
            :: (if (showExtras) p.queryExtras() else Future.successful(Seq()))
            :: (if (showResponse) responseCache.getOrElseUpdate(p.responseId, p.queryResponse()) else Future.successful(None))
            :: HNil
        )) awaitAll)
      yield (ping, product, extras, response)
  }
}
