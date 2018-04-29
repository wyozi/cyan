package model

import cyan.backend.model.BackendProduct
import dao.{PingsDAO, ProductConfigDAO}

import scala.concurrent.Future

/**
  * Created by wyozi on 3.2.2016.
  */
case class Product(id: Int, name: String, shortName: String) {
  def queryEstimatedLicenseCount()(implicit pingsDAO: PingsDAO): Future[Int] = pingsDAO.findEstimatedLicenseCount(this)
  def queryEstimatedPingCount()(implicit pingsDAO: PingsDAO): Future[Int] = pingsDAO.findEstimatedPingCount(this)
  def queryRecentPings(limit: Int)(implicit pingsDAO: PingsDAO): Future[Seq[Ping]] = pingsDAO.findRecentForProduct(this, limit)

  def queryDevLicense()(implicit productConfigDAO: ProductConfigDAO): Future[Option[String]] =
    productConfigDAO.getValue(id, ProductConfig.Keys.DevLicense)

  def backend()(implicit productConfigDAO: ProductConfigDAO) = new BackendProduct {
    override def name: String = Product.this.name
    override def id: Int = Product.this.id
    override def queryConfig(key: String): Future[Option[String]] = productConfigDAO.getValue(id, key)
  }
}