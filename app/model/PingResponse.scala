package model

import dao.ResponsesDAO

import scala.concurrent.Future

/**
  * Created by wyozi on 8.2.2016.
  */
case class PingResponse(id: Int, productId: Option[Int], license: Option[String], userName: Option[String], responseId: Option[Int]) {
  def queryResponse()(implicit responsesDAO: ResponsesDAO): Future[Option[Response]] =
    responseId.map(id => responsesDAO.findById(id)).getOrElse(Future.successful(None))
}
