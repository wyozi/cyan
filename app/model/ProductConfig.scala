package model

object ProductConfig {
  object Keys {
    val DevLicense = "devlicense"
    val PingCheckScript = "pingcheckscript"
  }
}
case class ProductConfig(productId: Int, key: String, value: String)