package cyan.util

import com.whisk.docker.{DockerContainer, DockerFactory, DockerKit}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}
import org.slf4j.LoggerFactory

import scala.concurrent.{Await, Future}

/**
  * DockerTestKit except rebuilds containers between each test
  */
trait DockerTestKitPerTest extends BeforeAndAfterEach with BeforeAndAfterAll with ScalaFutures {
  self: Suite with DockerKit =>

  private lazy val log = LoggerFactory.getLogger(this.getClass)

  private var kit = newKit()
  private def newKit(): DockerKit = new DockerKit {
    override implicit def dockerFactory: DockerFactory = self.dockerFactory
    override def dockerContainers = self.dockerContainers
  }

  override def beforeEach(): Unit = {
    super.beforeEach()

    kit = newKit()
    kit.startAllOrFail()
  }

  override def afterEach(): Unit = {
    val states = kit.containerManager.states

    // containerManager rmAll copied here because it closes the executor and we do not want that yet
    try {
      Await.ready(Future.traverse(states)(_.remove(force = true, removeVolumes = true)).map(_ => ()), StopContainersTimeout)
    } catch {
      case e: Throwable =>
        log.error(e.getMessage, e)
    }

    super.afterEach()
  }

  override def afterAll(): Unit = {
    // Executor closing code overridden; close manually after all tests
    kit.dockerExecutor.close()
    super.afterAll()
  }
}
