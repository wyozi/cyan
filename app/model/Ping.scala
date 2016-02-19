package model

import java.sql.Timestamp

import dao.{ProductsDAO, PingExtrasDAO, ResponsesDAO}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by wyozi on 3.2.2016.
  */
case class Ping(id: Int, date: Timestamp, product: String, license: String, user: String, ip: String, responseId: Option[Int]) {
  def queryProduct()(implicit ec: ExecutionContext, productsDAO: ProductsDAO): Future[Product] =
    // TODO make sure there are db constraints to make sure this never errors in scala
    productsDAO.findByShortName(product).map(_.get)

  def queryResponse()(implicit responsesDAO: ResponsesDAO): Future[Option[Response]] =
    responseId.map(id => responsesDAO.findById(id)).getOrElse(Future.successful(None))

  def queryExtras()(implicit pingExtrasDAO: PingExtrasDAO): Future[Seq[PingExtra]] =
    pingExtrasDAO.findExtras(id)

  def queryExtra(key: String)(implicit pingExtrasDAO: PingExtrasDAO): Future[Option[String]] =
    pingExtrasDAO.findValue(id, key)
}