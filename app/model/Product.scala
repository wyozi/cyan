package model

import dao.{ProdLicensePingDAO, ProductConfigDAO}

import scala.concurrent.Future

/**
  * Created by wyozi on 3.2.2016.
  */
case class Product(id: Int, name: String, shortName: String) {
  def queryLicenseCount()(implicit plpDAO: ProdLicensePingDAO) = plpDAO.findLicenseCount(this)
  def queryPingCount()(implicit plpDAO: ProdLicensePingDAO) = plpDAO.findPingCount(this)
  def queryRecentPings(limit: Int)(implicit plpDAO: ProdLicensePingDAO) = plpDAO.findRecentPings(this, limit)

  def queryDevLicense()(implicit productConfigDAO: ProductConfigDAO): Future[Option[String]] =
    productConfigDAO.getValue(id, ProductConfig.Keys.DevLicense)
}