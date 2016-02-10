package cyan.controllers

import controllers.PingReceiver
import cyan.DBApplication
import dao.{PingsDAO, ProductsDAO}
import model.{Product, Ping}
import play.api.mvc.Results
import play.api.test._

import util.FutureUtils._

/**
  * Created by wyozi on 10.2.2016.
  */
class PingReceiverSpec extends PlaySpecification with Results {
  "PingReceiver" should {
    "fail on invalid requests" in new WithApplication(FakeApplication()) {
      val pingReceiver = app.injector.instanceOf[PingReceiver]

      // no form body at all
      status(pingReceiver.ping()(FakeRequest(Helpers.POST, "/ping"))) must be equalTo BAD_REQUEST

      // empty form body
      status(pingReceiver.ping()(
        FakeRequest(Helpers.POST, "/ping").withFormUrlEncodedBody()
      )) must be equalTo BAD_REQUEST

      // missing parameters
      status(pingReceiver.ping()(
        FakeRequest(Helpers.POST, "/ping")
          .withFormUrlEncodedBody(("user", "Mike"))
      )) must be equalTo BAD_REQUEST

      status(pingReceiver.ping()(
        FakeRequest(Helpers.POST, "/ping")
          .withFormUrlEncodedBody(("user", "Mike"), ("license", "xlicense"))
      )) must be equalTo BAD_REQUEST

      status(pingReceiver.ping()(
        FakeRequest(Helpers.POST, "/ping")
          .withFormUrlEncodedBody(("user", "Mike"), ("prod", "banana"))
      )) must be equalTo BAD_REQUEST
    }
    "fail on requests that map to invalid database entries" in new DBApplication {
      val pingReceiver = app.injector.instanceOf[PingReceiver]

      // missing product
      status(pingReceiver.ping()(
        FakeRequest(Helpers.POST, "/ping")
          .withFormUrlEncodedBody(("user", "Mike"), ("license", "xlicense"), ("prod", "banana"))
      )) must be equalTo BAD_REQUEST
    }
    "succeed on valid requests" in new DBApplication {
      val productsDAO = app.injector.instanceOf[ProductsDAO]
      val pingsDAO = app.injector.instanceOf[PingsDAO]
      val pingReceiver = app.injector.instanceOf[PingReceiver]

      val productId = productsDAO.insert("banana", "banana").await()

      status(pingReceiver.ping()(
        FakeRequest(Helpers.POST, "/ping")
          .withFormUrlEncodedBody(("user", "Mike"), ("license", "xlicense"), ("prod", "banana"))
      )) must be equalTo OK

      val prodPings = pingsDAO.findRecentForProduct(Product(productId, "banana", "banana")).await
      prodPings.size must be equalTo 1
      prodPings.head must beAnInstanceOf[Ping]
      prodPings.head.user must be equalTo "Mike"
      prodPings.head.license must be equalTo "xlicense"
      prodPings.head.product must be equalTo "banana"
    }
  }
}
