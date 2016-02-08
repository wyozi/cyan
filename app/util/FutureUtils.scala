package util

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/**
  * Created by wyozi on 8.2.2016.
  */
object FutureUtils {
  implicit class FutureSync[T](future: Future[T]) {
    def await() = Await.result(future, 2.seconds)
  }
}
