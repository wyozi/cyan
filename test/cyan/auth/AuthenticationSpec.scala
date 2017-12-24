package cyan.auth

import java.nio.charset.StandardCharsets

import auth.Authentication
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.db.DBApi
import play.api.db.evolutions.EvolutionsModule
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{DefaultActionBuilder, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext

class AuthenticationSpec extends PlaySpec with Results with MockitoSugar {
  // Create app without db
  val app = new GuiceApplicationBuilder()
      .configure("cyan.password" -> "unittestpassword")
      .overrides(play.api.inject.bind[DBApi].toInstance(mock[DBApi]))
      .disable[EvolutionsModule]
      .build()

  private implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  private val auth = app.injector.instanceOf[Authentication]
  private val actionBuilder = app.injector.instanceOf[DefaultActionBuilder]

  private def createAuthHeader(user: String, passwd: String) = s"Basic ${org.apache.commons.codec.binary.Base64.encodeBase64String(s"$user:$passwd".getBytes(StandardCharsets.US_ASCII))}"

  private val blank = actionBuilder { Ok("") }


  "Authentication" must {

    "fail on nil authentications" in {
      status(
        auth.adminOnly(blank).apply(
          FakeRequest(controllers.admin.routes.Main.index())
        )
      ) mustEqual UNAUTHORIZED
    }

    "fail on invalid authentications" in {
      status(
        auth.adminOnly(blank).apply(
          FakeRequest(controllers.admin.routes.Main.index())
            .withHeaders(
              "Authorization" -> createAuthHeader("foo", "bar")
            )
        )
      ) mustEqual UNAUTHORIZED

      status(
        auth.adminOnly(blank).apply(
          FakeRequest(controllers.admin.routes.Main.index())
            .withHeaders(
              "Authorization" -> createAuthHeader(Authentication.AdminUsername, "hunter2")
            )
        )
      ) mustEqual UNAUTHORIZED

      status(
        auth.adminOnly(blank).apply(
          FakeRequest(controllers.admin.routes.Main.index())
            .withHeaders(
              "Authorization" -> createAuthHeader("user", "unittestpassword")
            )
        )
      ) mustEqual UNAUTHORIZED

      status(
        auth.adminOnly(blank).apply(
          FakeRequest(controllers.admin.routes.Main.index())
            .withHeaders(
              "Authorization" -> "aaaa"
            )
        )
      ) mustEqual UNAUTHORIZED
    }

    "succeed on valid authentications" in {

      status(
        auth.adminOnly(blank).apply(
          FakeRequest(controllers.admin.routes.Main.index())
            .withHeaders(
              "Authorization" -> createAuthHeader(Authentication.AdminUsername, "unittestpassword")
            )
        )
      ) mustEqual OK
    }
  }
}
