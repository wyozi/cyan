package backend

import java.io.File
import java.net.URLClassLoader

import com.google.inject.AbstractModule
import cyan.backend.Backend
import cyan.backend.impl.DefaultBackend
import play.api.{Configuration, Environment, Logger}

/**
  * Created by wyozi on 6.2.2016.
  */
class BackendModule(environment: Environment, configuration: Configuration) extends AbstractModule {
  def classloader: URLClassLoader = {
    val jarUrls = new File("extensions/").listFiles.filter(_.getName.endsWith(".jar")).map(_.toURI.toURL)

    val beClassDir = configuration.getString("cyan.backend.classpath")
    val dirUrls = beClassDir.map(s => Seq(new File(s).toURI.toURL)).getOrElse(Seq())

    val lookUrls = jarUrls ++ dirUrls
    Logger.info("Backend classpath URLs: " + lookUrls.mkString(","))

    new URLClassLoader(lookUrls, environment.classLoader)
  }

  override def configure(): Unit = {
    val be = configuration.getString("cyan.backend.class")

    val backendClass =
      be.flatMap[Class[_ <: Backend]] { cls =>
        try {
          Some(
            classloader
              .loadClass(cls)
              .asSubclass(classOf[Backend])
          )
        } catch {
          case e: Exception =>
            Logger.error(s"Failed to load Cyan backend class '$cls'", e)
            None
        }
      }.getOrElse(classOf[DefaultBackend])

    bind(classOf[Backend]).to(backendClass)
  }
}
