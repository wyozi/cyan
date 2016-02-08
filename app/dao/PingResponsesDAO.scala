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
class PingResponsesDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider, responsesDAO: ResponsesDAO)
  extends HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  private val PingResponses = TableQuery[PingResponsesTable]

  /*

  import play.api.Play.current


  private def getResponse(pingResponseId: Int): Option[Response] = {
      db.withConnection { implicit connection =>
        SQL(
          """
            |SELECT *
            |FROM Responses
            |WHERE id = (SELECT response_id FROM PingResponses WHERE id = {prid})
            |LIMIT 1
          """.stripMargin)
          .on('prid -> pingResponseId)
          .as(Response.Parser.singleOpt)
      }
  }

  def getBestResponse(productId: Option[Int], license: Option[String], user: Option[String]): Option[Response] = {
    getBestPingResponseId(productId, license, user).flatMap(getResponse)
  }
  def getExactResponse(productId: Option[Int], license: Option[String], user: Option[String]): Option[Response] = {
    getExactPingResponseId(productId, license, user).flatMap(getResponse)
  }

  def upsertExactPingResponse(productId: Option[Int], license: Option[String], user: Option[String], response: Option[Int]): Unit = {
    db.withConnection { implicit connection =>
      getExactPingResponseId(productId, license, user).fold(
        // empty
        SQL("INSERT INTO PingResponses(product_id, license, user_name, response_id) VALUES ({prod}, {license}, {user}, {resp})")
          .on('prod -> productId, 'license -> license, 'user -> user, 'resp -> response)
          .execute()
      ) { id =>
        // found
        SQL("UPDATE PingResponses SET response_id = {resp} WHERE id = {prid}")
          .on('prid -> id, 'resp -> response)
          .execute()
      }
    }
  }
 */

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
  def getBestPingResponseId(productId: Option[Int], license: Option[String], user: Option[String]): Future[Option[Int]] = {
    db.run(
      (
        PingResponses.filter(pr => pr.productId === productId && pr.license === license && pr.userName === user).map(_.id)
        ++
        PingResponses.filter(pr => pr.userName === user).map(_.id)
        ++
        PingResponses.filter(pr => pr.productId === productId && pr.license === license && pr.userName.isEmpty).map(_.id)
        ++
        PingResponses.filter(pr => pr.productId === productId && pr.license.isEmpty && pr.userName.isEmpty).map(_.id)
      ).result.headOption
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
  def getExactPingResponseId(productId: Option[Int], license: Option[String], user: Option[String]): Future[Option[Int]] = {
    val q = (productId, license, user) match {
      case (Some(p), Some(l), Some(u)) => PingResponses.filter(pr => pr.productId === productId && pr.license === license && pr.userName === user).map(_.id)
      case (None, None, Some(u)) => PingResponses.filter(pr => pr.productId.isEmpty && pr.license.isEmpty && pr.userName === user).map(_.id)
      case (Some(p), Some(l), None) =>PingResponses.filter(pr => pr.productId === productId && pr.license === license && pr.userName.isEmpty).map(_.id)
      case (Some(p), None, None) => PingResponses.filter(pr => pr.productId === productId && pr.license.isEmpty && pr.userName.isEmpty).map(_.id)
      case params => throw new RuntimeException(s"cannot search for exact ping response with this list of params: $params")
    }
    db.run(q.result.headOption)
  }

  def getBestResponse(productId: Option[Int], license: Option[String], user: Option[String]): Option[Response] = {
    ???
  }
  def getExactResponse(productId: Option[Int], license: Option[String], user: Option[String]): Future[Option[Response]] = {
    getExactPingResponseId(productId, license, user).flatMap {
      case Some(prid) => responsesDAO.findById(prid)
    }
  }

  def upsertExactPingResponse(productId: Option[Int], license: Option[String], user: Option[String], response: Option[Int]): Unit = ???

  private class PingResponsesTable(tag: Tag) extends Table[PingResponse](tag, "PINGRESPONSES") {
    def id = column[Int]("ID", O.AutoInc)

    def productId = column[Option[Int]]("PRODUCT_ID")
    def license = column[Option[String]]("LICENSE")
    def userName = column[Option[String]]("USER_NAME")

    def responseId = column[Option[Int]]("RESPONSE_ID")

    override def * = (id, productId, license, userName, responseId) <> (PingResponse.tupled, PingResponse.unapply)
  }
}
