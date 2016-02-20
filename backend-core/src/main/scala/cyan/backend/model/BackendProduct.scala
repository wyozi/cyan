package cyan.backend.model

import scala.concurrent.Future

trait BackendProduct {

  /**
    * Readable name of the product
    *
    * @return
    */
  def name: String

  /**
    * Numeric product id
    * @return
    */
  def id: Int

  /**
    * Queries backend product for a configuration value. The `key` parameter is equal to CustomProjectConfig#id
    *
    * @param key the configuration key to query for
    * @return
    */
  def queryConfig(key: String): Future[Option[String]]
}
