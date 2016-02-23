package cyan.controllers

import controllers.PingReceiver
import cyan.DBSpec
import dao.{PingsDAO, ProductsDAO}
import model.Product
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Results
import play.api.test.Helpers._
import play.api.test._

/**
  * Created by wyozi on 10.2.2016.
  */
class PingReceiverSpec extends PlaySpec with Results with OneAppPerSuite with DBSpec {

  implicit override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(inMemorySlickDatabase())
    .build()

  "PingReceiver" should {
    "fail on invalid requests" in {
      val pingReceiver = app.injector.instanceOf[PingReceiver]

      // no form body at all
      status(pingReceiver.ping()(FakeRequest(Helpers.POST, "/ping"))) mustEqual BAD_REQUEST

      // empty form body
      status(pingReceiver.ping()(
        FakeRequest(Helpers.POST, "/ping").withFormUrlEncodedBody()
      )) mustEqual BAD_REQUEST

      // missing parameters
      status(pingReceiver.ping()(
        FakeRequest(Helpers.POST, "/ping")
          .withFormUrlEncodedBody(("user", "Mike"))
      )) mustEqual BAD_REQUEST

      status(pingReceiver.ping()(
        FakeRequest(Helpers.POST, "/ping")
          .withFormUrlEncodedBody(("user", "Mike"), ("license", "xlicense"))
      )) mustEqual BAD_REQUEST

      status(pingReceiver.ping()(
        FakeRequest(Helpers.POST, "/ping")
          .withFormUrlEncodedBody(("user", "Mike"), ("prod", "banana"))
      )) mustEqual BAD_REQUEST
    }
    "fail on requests that map to invalid database entries" in {
      val pingReceiver = app.injector.instanceOf[PingReceiver]

      // missing product
      status(pingReceiver.ping()(
        FakeRequest(Helpers.POST, "/ping")
          .withFormUrlEncodedBody(("user", "Mike"), ("license", "xlicense"), ("prod", "banana"))
      )) mustEqual BAD_REQUEST
    }
    "succeed on valid requests" in {
      val productsDAO = app.injector.instanceOf[ProductsDAO]
      val pingsDAO = app.injector.instanceOf[PingsDAO]
      val pingReceiver = app.injector.instanceOf[PingReceiver]

      val productId = await(productsDAO.insert("banana", "banana"))

      status(pingReceiver.ping()(
        FakeRequest(Helpers.POST, "/ping")
          .withFormUrlEncodedBody(("user", "Mike"), ("license", "xlicense"), ("prod", "banana"))
      )) mustEqual OK

      val prodPings = await(pingsDAO.findRecentForProduct(Product(productId, "banana", "banana")))
      prodPings.size mustEqual 1
      prodPings.head.user mustEqual "Mike"
      prodPings.head.license mustEqual "xlicense"
      prodPings.head.product mustEqual "banana"
    }
  }
}
