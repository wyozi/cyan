package cyan.dao

import cyan.DBSpec
import dao.{PingResponsesDAO, ResponsesDAO}
import org.scalatest.TestData
import org.scalatestplus.play.{OneAppPerTest, PlaySpec}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import util.FutureUtils._

/**
  * Created by wyozi on 5.2.2016.
  */
class PingResponseDAOSpec extends PlaySpec with OneAppPerTest with DBSpec {
  implicit override def newAppForTest(td: TestData): Application =
    new GuiceApplicationBuilder()
      .configure(inMemorySlickDatabase())
      .build()

  "PingResponseRepository" should {
    "upsert ping response" in new App(){
      val responsesDAO = app.injector.instanceOf[ResponsesDAO]
      val pingResponsesDAO = app.injector.instanceOf[PingResponsesDAO]

      // need these for database integrity
      val resp1 = await(responsesDAO.insert("", ""))
      val resp2 = await(responsesDAO.insert("", ""))

      // Test adding more and more exact ping responses and checking result
      await(pingResponsesDAO.pingResponseCount) mustEqual 0

      val id1 = await(pingResponsesDAO.upsertExactPingResponse(None, None, Some("Mike"), Some(resp1)))
      await(pingResponsesDAO.pingResponseCount) mustEqual 1
      val pr1 = await(pingResponsesDAO.getPingResponse(id1))
      pr1 mustBe defined
      pr1.get.responseId mustBe Some(resp1)

      val id2 = await(pingResponsesDAO.upsertExactPingResponse(None, None, Some("Mike"), Some(resp2)))
      await(pingResponsesDAO.pingResponseCount) mustEqual 1
      val pr2 = await(pingResponsesDAO.getPingResponse(id2))
      pr2 mustBe defined
      pr2.get.responseId mustBe Some(resp2)

      val id3 = await(pingResponsesDAO.upsertExactPingResponse(None, None, Some("Mike"), None))
      await(pingResponsesDAO.pingResponseCount) mustEqual 1 // TODO: should this remove the row?
      val pr3 = await(pingResponsesDAO.getPingResponse(id3))
      pr3 must not be defined
    }
    "return correct exact ping response ids" in {
      val responsesDAO = app.injector.instanceOf[ResponsesDAO]
      val pingResponsesDAO = app.injector.instanceOf[PingResponsesDAO]

      // Insert test data
      val fullRespId = await(responsesDAO.insert("test", "full"))
      val frid = await(pingResponsesDAO.upsertExactPingResponse(Some(1), Some("license"), Some("user"), Some(fullRespId)))
      val nouserRespId = await(responsesDAO.insert("test", "nouser"))
      await(pingResponsesDAO.upsertExactPingResponse(Some(1), Some("license"), None,         Some(nouserRespId)))
      val onlyprodRespId = await(responsesDAO.insert("test", "onlyprod"))
      await(pingResponsesDAO.upsertExactPingResponse(Some(1), None           , None,         Some(onlyprodRespId)))

      // Test added responses
      await(pingResponsesDAO.getExactPingResponse(Some(1), Some("license"), Some("user"))).flatMap(_.responseId) mustEqual Some(fullRespId)
      await(pingResponsesDAO.getExactPingResponse(Some(1), Some("license"), None)).flatMap(_.responseId) mustEqual Some(nouserRespId)
      await(pingResponsesDAO.getExactPingResponse(Some(1), None, None)).flatMap(_.responseId) mustEqual Some(onlyprodRespId)

      // Test single-column checks that do not exist
      await(pingResponsesDAO.getExactPingResponse(None, None, Some("user"))) mustEqual None

      // Test added single-column checks
      val onlyuserRespId = await(responsesDAO.insert("test", "onlyuser"))
      await(pingResponsesDAO.upsertExactPingResponse(None, None, Some("user"), Some(onlyuserRespId)))
      await(pingResponsesDAO.getExactPingResponse(None, None, Some("user"))).flatMap(_.responseId) mustEqual Some(onlyuserRespId)
    }

    "return correct best ping response ids" in {
      val responsesDAO = app.injector.instanceOf[ResponsesDAO]
      val pingResponsesDAO = app.injector.instanceOf[PingResponsesDAO]

      // Test adding more and more exact ping responses and checking result

      val onlyprodRespId = responsesDAO.insert("test", "onlyprod").await()
      val upserted = pingResponsesDAO.upsertExactPingResponse(Some(1), None, None, Some(onlyprodRespId)).await()

      pingResponsesDAO.getBestPingResponse(Some(1), Some("license"), Some("user")).await().flatMap(_.responseId) mustEqual Some(onlyprodRespId)

      val prodlicenseRespId = responsesDAO.insert("test", "prodlicense").await()
      pingResponsesDAO.upsertExactPingResponse(Some(1), Some("license"), None, Some(prodlicenseRespId)).await()

      pingResponsesDAO.getBestPingResponse(Some(1), Some("license"), Some("user")).await().flatMap(_.responseId) mustEqual Some(prodlicenseRespId)

      val fullRespId = responsesDAO.insert("test", "full").await()
      pingResponsesDAO.upsertExactPingResponse(Some(1), Some("license"), Some("user"), Some(fullRespId)).await()

      pingResponsesDAO.getBestPingResponse(Some(1), Some("license"), Some("user")).await().flatMap(_.responseId) mustEqual Some(fullRespId)

      // Make sure we don't return a Null response if there's a better response available
      pingResponsesDAO.upsertExactPingResponse(Some(1), Some("license"), Some("user2"), None).await()
      pingResponsesDAO.getBestPingResponse(Some(1), Some("license"), Some("user2")).await().flatMap(_.responseId) mustEqual Some(prodlicenseRespId)
    }
  }
}
