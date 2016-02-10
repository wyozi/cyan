package dao

import com.google.inject.Inject
import model.{PingResponse, Response}
import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by wyozi on 8.2.2016.
  */
class PingResponsesDAO @Inject() (responsesDAO: ResponsesDAO)(protected implicit val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  private[dao] val PingResponses = TableQuery[PingResponsesTable]

  def pingResponseCount: Future[Int] = db.run(PingResponses.length.result)

  /**
    * Gets the ping response id that best matches given parameters.
    * Checks following in order:
    *  - product/licence match and user match
    *  - user match
    *  - product/licence match (with null user)
    *  - product match (with null license and user)
    *
    * @param productId
    * @param license
    * @param user
    * @return
    */
  def getBestPingResponse(productId: Option[Int], license: Option[String], user: Option[String]): Future[Option[PingResponse]] = {
    db.run(
      (
        PingResponses.filter(pr => pr.responseId.isDefined && pr.productId === productId && pr.license === license && pr.userName === user)
        ++
        PingResponses.filter(pr => pr.responseId.isDefined && pr.userName === user)
        ++
        PingResponses.filter(pr => pr.responseId.isDefined && pr.productId === productId && pr.license === license && pr.userName.isEmpty)
        ++
        PingResponses.filter(pr => pr.responseId.isDefined && pr.productId === productId && pr.license.isEmpty && pr.userName.isEmpty)
      ).take(1).result.headOption
    )
  }

  /**
    * Gets the ping response that matches given parameters exactly.
    *
    * @param productId
    * @param license
    * @param user
    * @return
    */
  def getExactPingResponse(productId: Option[Int], license: Option[String], user: Option[String]): Future[Option[PingResponse]] = {
    val q = (productId, license, user) match {
      case (Some(p), Some(l), Some(u)) => PingResponses.filter(pr => pr.productId === productId && pr.license === license && pr.userName === user)
      case (None, None, Some(u)) => PingResponses.filter(pr => pr.productId.isEmpty && pr.license.isEmpty && pr.userName === user)
      case (Some(p), Some(l), None) =>PingResponses.filter(pr => pr.productId === productId && pr.license === license && pr.userName.isEmpty)
      case (Some(p), None, None) => PingResponses.filter(pr => pr.productId === productId && pr.license.isEmpty && pr.userName.isEmpty)
      case params => throw new RuntimeException(s"cannot search for exact ping response with this list of params: $params")
    }
    db.run(q.take(1).result.headOption)
  }

  def getBestResponse(productId: Option[Int], license: Option[String], user: Option[String]): Future[Option[Response]] = {
    getBestPingResponse(productId, license, user).flatMap {
      case Some(pr) if pr.responseId.isDefined => responsesDAO.findById(pr.responseId.get)
      case _ => Future.successful(None)
    }
  }
  def getExactResponse(productId: Option[Int], license: Option[String], user: Option[String]): Future[Option[Response]] = {
    getExactPingResponse(productId, license, user).flatMap {
      case Some(pr) if pr.responseId.isDefined => responsesDAO.findById(pr.responseId.get)
      case _ => Future.successful(None)
    }
  }

  def upsertExactPingResponse(productId: Option[Int], license: Option[String], user: Option[String], responseId: Option[Int]): Future[Unit] = {
    getExactPingResponse(productId, license, user).map {
      case Some(pr) => db.run(sqlu"UPDATE PingResponses SET response_id = ${responseId} WHERE id = ${pr.id}")
      case None => db.run(sqlu"INSERT INTO PingResponses(product_id, license, user_name, response_id) VALUES (${productId}, ${license}, ${user}, ${responseId})")
    }
  }

  private[dao] class PingResponsesTable(tag: Tag) extends Table[PingResponse](tag, "PingResponses") {
    def id = column[Int]("ID", O.AutoInc)

    def productId = column[Option[Int]]("PRODUCT_ID")
    def license = column[Option[String]]("LICENSE")
    def userName = column[Option[String]]("USER_NAME")

    def responseId = column[Option[Int]]("RESPONSE_ID")

    override def * = (id, productId, license, userName, responseId) <> (PingResponse.tupled, PingResponse.unapply)
  }
}
