package cyan

trait DBSpec {
  def inMemorySlickDatabase(name: String = "default", options: Map[String, String] = Map.empty[String, String]): Map[String, String] = {
    val optionsForDbUrl = options.map { case (k, v) => k + "=" + v }.mkString(";", ";", "")

    Map(
      ("slick.dbs." + name + ".driver") -> "slick.driver.H2Driver$",
      ("slick.dbs." + name + ".db.driver") -> "org.h2.Driver",
      ("slick.dbs." + name + ".db.url") -> ("jdbc:h2:mem:play-test-" + scala.util.Random.nextInt + optionsForDbUrl)
    )
  }
}
