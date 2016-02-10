package cyan.dao

import cyan.DBApplication
import dao.{PingResponsesDAO, ResponsesDAO}
import play.api.test.PlaySpecification
import util.FutureUtils._

/**
  * Created by wyozi on 5.2.2016.
  */
class PingResponseDAOSpec extends PlaySpecification {
  "PingResponseRepository" should {
    "upsert ping response" in new DBApplication{
      val responsesDAO = new ResponsesDAO()
      val pingResponsesDAO = new PingResponsesDAO(responsesDAO)

      // need these for database integrity
      val resp1 = responsesDAO.insert("", "").await()
      val resp2 = responsesDAO.insert("", "").await()

      // Test adding more and more exact ping responses and checking result
      pingResponsesDAO.pingResponseCount.await() must be equalTo 0

      pingResponsesDAO.upsertExactPingResponse(None, None, Some("Mike"), Some(resp1)).await()
      pingResponsesDAO.pingResponseCount.await() must be equalTo 1

      pingResponsesDAO.upsertExactPingResponse(None, None, Some("Mike"), Some(resp2)).await()
      pingResponsesDAO.pingResponseCount.await() must be equalTo 1

      pingResponsesDAO.upsertExactPingResponse(None, None, Some("Mike"), None).await()
      pingResponsesDAO.pingResponseCount.await() must be equalTo 1 // TODO: should this remove the row?
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
      pingResponsesDAO.getExactPingResponse(Some(1), Some("license"), Some("user")).await().flatMap(_.responseId) must be equalTo Some(fullRespId)
      pingResponsesDAO.getExactPingResponse(Some(1), Some("license"), None).await().flatMap(_.responseId) must be equalTo Some(nouserRespId)
      pingResponsesDAO.getExactPingResponse(Some(1), None, None).await().flatMap(_.responseId) must be equalTo Some(onlyprodRespId)

      // Test single-column checks that do not exist
      pingResponsesDAO.getExactPingResponse(None, None, Some("user")).await() must be equalTo None

      // Test added single-column checks
      val onlyuserRespId = responsesDAO.insert("test", "onlyuser").await()
      pingResponsesDAO.upsertExactPingResponse(None, None, Some("user"), Some(onlyuserRespId)).await()
      pingResponsesDAO.getExactPingResponse(None, None, Some("user")).await().flatMap(_.responseId) must be equalTo Some(onlyuserRespId)
    }

    "return correct best ping response ids" in new DBApplication{
      val responsesDAO = new ResponsesDAO()
      val pingResponsesDAO = new PingResponsesDAO(responsesDAO)

      // Test adding more and more exact ping responses and checking result

      val onlyprodRespId = responsesDAO.insert("test", "onlyprod").await()
      pingResponsesDAO.upsertExactPingResponse(Some(1), None, None, Some(onlyprodRespId)).await()

      pingResponsesDAO.getBestPingResponse(Some(1), Some("license"), Some("user")).await().flatMap(_.responseId) must be equalTo Some(onlyprodRespId)

      val prodlicenseRespId = responsesDAO.insert("test", "prodlicense").await()
      pingResponsesDAO.upsertExactPingResponse(Some(1), Some("license"), None, Some(prodlicenseRespId)).await()

      pingResponsesDAO.getBestPingResponse(Some(1), Some("license"), Some("user")).await().flatMap(_.responseId) must be equalTo Some(prodlicenseRespId)

      val fullRespId = responsesDAO.insert("test", "full").await()
      pingResponsesDAO.upsertExactPingResponse(Some(1), Some("license"), Some("user"), Some(fullRespId)).await()

      pingResponsesDAO.getBestPingResponse(Some(1), Some("license"), Some("user")).await().flatMap(_.responseId) must be equalTo Some(fullRespId)

      // Make sure we don't return a Null response if there's a better response available
      pingResponsesDAO.upsertExactPingResponse(Some(1), Some("license"), Some("user2"), None).await()
      pingResponsesDAO.getBestPingResponse(Some(1), Some("license"), Some("user2")).await().flatMap(_.responseId) must be equalTo Some(prodlicenseRespId)
    }
  }
}
