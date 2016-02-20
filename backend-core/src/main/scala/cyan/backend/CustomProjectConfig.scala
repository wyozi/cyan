package cyan.backend

class CustomProjectConfig(val desc: String, val id: String)
object CustomProjectConfig {
  def apply(desc: String, id: String): CustomProjectConfig = new CustomProjectConfig(desc, id)
}
