package cyan.util

import java.sql.DriverManager

import com.whisk.docker.impl.spotify.DockerKitSpotify
import com.whisk.docker.{DockerCommandExecutor, DockerContainer, DockerContainerState, DockerKit, DockerReadyChecker}

import scala.concurrent.ExecutionContext
import scala.util.Try

trait DockerPostgresService extends DockerKitSpotify {
  import scala.concurrent.duration._
  private def PostgresAdvertisedPort = 5432
  private def PostgresExposedPort = 44444
  private val PostgresUser = "cyantest"
  private val PostgresPassword = "cyan"
  private val PostgresDatabase = "cyan"

  private val postgresContainer = DockerContainer("postgres:9.5")
    .withPorts((PostgresAdvertisedPort, Some(PostgresExposedPort)))
    .withEnv(s"POSTGRES_USER=$PostgresUser", s"POSTGRES_PASSWORD=$PostgresPassword", s"POSTGRES_DB=$PostgresDatabase")
    .withReadyChecker(
      new PostgresReadyChecker(PostgresUser, PostgresPassword, PostgresDatabase, Some(PostgresExposedPort))
        .looped(15, 1.second)
    )

  val dbConfig = Map(
      "slick.dbs.default.db.url" -> s"jdbc:postgresql://localhost:$PostgresExposedPort/$PostgresDatabase",
      "slick.dbs.default.db.user" -> PostgresUser,
      "slick.dbs.default.db.password" -> PostgresPassword
    )

  abstract override def dockerContainers: List[DockerContainer] =
    postgresContainer :: super.dockerContainers
}

class PostgresReadyChecker(user: String, password: String, db: String, port: Option[Int] = None)
  extends DockerReadyChecker {

  override def apply(container: DockerContainerState)(implicit docker: DockerCommandExecutor,
                                                      ec: ExecutionContext) =
    container
      .getPorts()
      .map(ports =>
        Try {
          Class.forName("org.postgresql.Driver")
          val url = s"jdbc:postgresql://localhost:${port.getOrElse(ports.values.head)}/$db"
          Option(DriverManager.getConnection(url, user, password)).map(_.close).isDefined
        }.getOrElse(false))
}
