import DbUtils.TestDatabase
import dbrepo.PingResponseRepository
import org.scalatestplus.play._

/**
  * Created by wyozi on 5.2.2016.
  */
class PingResponseRepoSpec extends PlaySpec {
  "PingResponseRepository" should {
    "return correct exact ping response ids" in {
      val db = DbUtils.newDatabase()
      val prr = new PingResponseRepository(db)

      val fullRespId = db.insertResponse("test", "full")
      db.insertPingResponse(Some(1), Some("license"), Some("user"), Some(fullRespId))
      val nouserRespId = db.insertResponse("test", "nouser")
      db.insertPingResponse(Some(1), Some("license"), None,         Some(nouserRespId))
      val onlyprodRespId = db.insertResponse("test", "onlyprod")
      db.insertPingResponse(Some(1), None           , None,         Some(onlyprodRespId))

      // Test added responses
      prr.getExactPingResponseId(Some(1), Some("license"), Some("user")) mustBe Some(fullRespId)
      prr.getExactPingResponseId(Some(1), Some("license"), None) mustBe Some(nouserRespId)
      prr.getExactPingResponseId(Some(1), None, None) mustBe Some(onlyprodRespId)

      // Test single-column checks that do not exist
      prr.getExactPingResponseId(None, None, Some("user")) mustBe (None)

      // Test added single-column checks
      val onlyuserRespId = db.insertResponse("test", "onlyuser")
      db.insertPingResponse(None, None, Some("user"), Some(onlyuserRespId))
      prr.getExactPingResponseId(None, None, Some("user")) mustBe Some(onlyuserRespId)

      db.shutdown()
    }

    "return correct best ping response ids" in {
      val db = DbUtils.newDatabase()
      val prr = new PingResponseRepository(db)

      val onlyprodRespId = db.insertResponse("test", "onlyprod")
      db.insertPingResponse(Some(1), None, None, Some(onlyprodRespId))

      prr.getBestPingResponseId(Some(1), Some("license"), Some("user")) mustBe Some(onlyprodRespId)

      val prodlicenseRespId = db.insertResponse("test", "prodlicense")
      db.insertPingResponse(Some(1), Some("license"), None, Some(prodlicenseRespId))

      prr.getBestPingResponseId(Some(1), Some("license"), Some("user")) mustBe Some(prodlicenseRespId)

      val fullRespId = db.insertResponse("test", "full")
      db.insertPingResponse(Some(1), Some("license"), Some("user"), Some(fullRespId))

      prr.getBestPingResponseId(Some(1), Some("license"), Some("user")) mustBe Some(fullRespId)

      db.shutdown()
    }
  }
}
