package views.snippet

import cyan.backend.Backend
import dao.{PingExtrasDAO, ProductConfigDAO, ProductsDAO, ResponsesDAO}
import model.{Ping, PingExtra, Product, Response}

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * Contains utilities for use with ping_table view
  */
object PingTable {
  import util.FutureUtils._
  def fetch(pings: Seq[Ping], showExtras: Boolean, showResponse: Boolean)(implicit productsDAO: ProductsDAO, responsesDAO: ResponsesDAO = null, pingExtrasDAO: PingExtrasDAO = null, productConfigDAO: ProductConfigDAO = null, backend: Backend = null, ec: ExecutionContext): Seq[(Ping, Product, Seq[PingExtra], Option[Response])] = {
    import shapeless._

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

  /**
    * Group values in `seq` that sequentially return the same value for `grouping(value)`
    * @return
    */
  def sequentialGrouping[T, U](seq: Seq[T], grouping: T => U): Seq[(U, Seq[T])] = {
    def split(list: Seq[T]) : Seq[(U, Seq[T])] = list match {
      case Nil => Nil
      case h +: _ =>
        val g = grouping(h)
        val segment = list takeWhile {g == grouping(_)}
        Seq((g, segment)) ++ split(list drop segment.length)
    }

    split(seq)
  }
}
