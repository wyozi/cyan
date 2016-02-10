package util

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * Created by wyozi on 8.2.2016.
  */
object FutureUtils {
  implicit class FutureSync[T](future: Future[T]) {
    def await(): T = Await.result(future, 10.seconds)
  }
}
