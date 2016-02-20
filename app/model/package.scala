import cyan.backend.model.{BackendProduct, BackendLicense}
import dao.ProductConfigDAO

package object model {
  // TODO: create a proper case class for license
  type License = String

  def backendLicense(prod: Product, l: License)(implicit productConfigDAO: ProductConfigDAO): BackendLicense = new BackendLicense {
    override def value: String = l
    override def product: BackendProduct = prod.backend
  }
}
