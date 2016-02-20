package cyan.backend.model

trait BackendLicense {
  /**
    * Value of the license. This is the id.
    *
    * @return
    */
  def value: String

  /**
    * The product this license is for.
    *
    * @return
    */
  def product: BackendProduct
}
