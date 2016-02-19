package model

import java.sql.Timestamp

import dao.ResponsesDAO

import scala.concurrent.Future

/**
  * Created by wyozi on 3.2.2016.
  */
case class Ping(id: Int, date: Timestamp, product: String, license: String, user: String, ip: String, responseId: Option[Int]) {
  def queryResponse()(implicit responsesDAO: ResponsesDAO): Future[Option[Response]] =
    responseId.map(id => responsesDAO.findById(id)).getOrElse(Future.successful(None))
}