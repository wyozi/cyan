package model

object ProductConfig {
  object Keys {
    val DevLicense = "devlicense"
  }
}
case class ProductConfig(productId: Int, key: String, value: String)