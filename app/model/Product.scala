package model

import dao.ProductConfigDAO

import scala.concurrent.Future

/**
  * Created by wyozi on 3.2.2016.
  */
case class Product(id: Int, name: String, shortName: String) {
  def queryDevLicense()(implicit productConfigDAO: ProductConfigDAO): Future[Option[String]] =
    productConfigDAO.getValue(id, ProductConfig.Keys.DevLicense)
}