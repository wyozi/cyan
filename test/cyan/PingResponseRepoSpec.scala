package cyan

import dao.{PingResponsesDAO, ResponsesDAO}
import org.scalatestplus.play.PlaySpec
import util.FutureUtils._

/**
  * Created by wyozi on 5.2.2016.
  */
class PingResponseRepoSpec extends PlaySpec {
  "PingResponseRepository" should {
    "upsert ping response" in new DBApplication{

      //Evolutions.applyFor("default")

      val responsesDAO = new ResponsesDAO()
      val pingResponsesDAO = new PingResponsesDAO(responsesDAO)

      // need these for database integrity
      val resp1 = responsesDAO.insert("", "").await()
      val resp2 = responsesDAO.insert("", "").await()

      // Test adding more and more exact ping responses and checking result
      pingResponsesDAO.pingResponseCount.await() mustBe 0

      pingResponsesDAO.upsertExactPingResponse(None, None, Some("Mike"), Some(resp1)).await()
      pingResponsesDAO.pingResponseCount.await() mustBe 1

      pingResponsesDAO.upsertExactPingResponse(None, None, Some("Mike"), Some(resp2)).await()
      pingResponsesDAO.pingResponseCount.await() mustBe 1

      pingResponsesDAO.upsertExactPingResponse(None, None, Some("Mike"), None).await()
      pingResponsesDAO.pingResponseCount.await() mustBe 1 // TODO: should this remove the row?
    }
    "return correct exact ping response ids" in new DBApplication{
      val responsesDAO = new ResponsesDAO()
      val pingResponsesDAO = new PingResponsesDAO(responsesDAO)

      // Insert test data
      val fullRespId = responsesDAO.insert("test", "full").await()
      pingResponsesDAO.upsertExactPingResponse(Some(1), Some("license"), Some("user"), Some(fullRespId)).await()
      val nouserRespId = responsesDAO.insert("test", "nouser").await()
      pingResponsesDAO.upsertExactPingResponse(Some(1), Some("license"), None,         Some(nouserRespId)).await()
      val onlyprodRespId = responsesDAO.insert("test", "onlyprod").await()
      pingResponsesDAO.upsertExactPingResponse(Some(1), None           , None,         Some(onlyprodRespId)).await()

      // Test added responses
      pingResponsesDAO.getExactPingResponse(Some(1), Some("license"), Some("user")).await().flatMap(_.responseId) mustBe Some(fullRespId)
      pingResponsesDAO.getExactPingResponse(Some(1), Some("license"), None).await().flatMap(_.responseId) mustBe Some(nouserRespId)
      pingResponsesDAO.getExactPingResponse(Some(1), None, None).await().flatMap(_.responseId) mustBe Some(onlyprodRespId)

      // Test single-column checks that do not exist
      pingResponsesDAO.getExactPingResponse(None, None, Some("user")).await() mustBe None

      // Test added single-column checks
      val onlyuserRespId = responsesDAO.insert("test", "onlyuser").await()
      pingResponsesDAO.upsertExactPingResponse(None, None, Some("user"), Some(onlyuserRespId)).await()
      pingResponsesDAO.getExactPingResponse(None, None, Some("user")).await().flatMap(_.responseId) mustBe Some(onlyuserRespId)
    }

    "return correct best ping response ids" in new DBApplication{
      val responsesDAO = new ResponsesDAO()
      val pingResponsesDAO = new PingResponsesDAO(responsesDAO)

      // Test adding more and more exact ping responses and checking result

      val onlyprodRespId = responsesDAO.insert("test", "onlyprod").await()
      pingResponsesDAO.upsertExactPingResponse(Some(1), None, None, Some(onlyprodRespId)).await()

      pingResponsesDAO.getBestPingResponse(Some(1), Some("license"), Some("user")).await().flatMap(_.responseId) mustBe Some(onlyprodRespId)

      val prodlicenseRespId = responsesDAO.insert("test", "prodlicense").await()
      pingResponsesDAO.upsertExactPingResponse(Some(1), Some("license"), None, Some(prodlicenseRespId)).await()

      pingResponsesDAO.getBestPingResponse(Some(1), Some("license"), Some("user")).await().flatMap(_.responseId) mustBe Some(prodlicenseRespId)

      val fullRespId = responsesDAO.insert("test", "full").await()
      pingResponsesDAO.upsertExactPingResponse(Some(1), Some("license"), Some("user"), Some(fullRespId)).await()

      pingResponsesDAO.getBestPingResponse(Some(1), Some("license"), Some("user")).await().flatMap(_.responseId) mustBe Some(fullRespId)

      // Make sure we don't return a Null response if there's a better response available
      pingResponsesDAO.upsertExactPingResponse(Some(1), Some("license"), Some("user2"), None).await()
      pingResponsesDAO.getBestPingResponse(Some(1), Some("license"), Some("user2")).await().flatMap(_.responseId) mustBe Some(prodlicenseRespId)
    }
  }
}
