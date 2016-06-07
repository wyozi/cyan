package util

import shapeless._
import shapeless.ops.hlist.Tupler
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.languageFeature.implicitConversions
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by wyozi on 8.2.2016.
  */
object FutureUtils extends App {
  val FutureTimeout = 10.minutes

  implicit class FutureExt[T](future: Future[T]) {
    def await(): T = Await.result(future, FutureTimeout)
  }
  implicit class CollectionFutureExt[T](future: Seq[Future[T]]) {
    def awaitAll(): Seq[T] = Await.result(Future.sequence(future), FutureTimeout)
  }

  trait IsHListOfFutures[In <: HList, Out <:HList ] {
    def hsequence(l : In)(implicit ec: ExecutionContext): Future[Out]
  }

  object IsHListOfFutures {
    def apply[In <: HList, Out <: HList](implicit isHzippable: IsHListOfFutures[In, Out]): IsHListOfFutures[In, Out] = isHzippable

    implicit object HNilIsListOfFutures extends IsHListOfFutures[HNil, HNil] {
      override def hsequence(l : HNil)(implicit ec: ExecutionContext): Future[HNil] = Future.successful(HNil)
    }

    implicit def hconsIsHListOfFutures[H, In <: HList, Out <: HList]
    (implicit ev: IsHListOfFutures[In, Out]): IsHListOfFutures[Future[H] :: In, H :: Out] = new IsHListOfFutures[Future[H] :: In, H :: Out] {

      override def hsequence(l : Future[H] :: In)(implicit ec: ExecutionContext): Future[H :: Out] = {
        val head = l.head
        val tail = l.tail
        head.flatMap(h => ev.hsequence(tail).map(h :: _))
      }
    }
  }

  def hsequence[In <: HList, Out <: HList](l : In)(implicit ev: IsHListOfFutures[In, Out], ec: ExecutionContext) = ev.hsequence(l)

  def zip[P <: Product, In <: HList, Out <: HList](p: P)
    (implicit gen: Generic.Aux[P, In], ev: IsHListOfFutures[In, Out], tupler: Tupler[Out], ec: ExecutionContext) = {

    hsequence(gen.to(p)).map(_.tupled)
  }
}
