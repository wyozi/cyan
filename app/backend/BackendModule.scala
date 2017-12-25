package backend

import java.io.File
import java.net.{JarURLConnection, URLClassLoader}
import java.util.jar.JarFile

import com.google.inject.AbstractModule
import cyan.backend.Backend
import cyan.backend.impl.DefaultBackend
import play.api.{Configuration, Environment, Logger}

/**
  * Created by wyozi on 6.2.2016.
  */
class BackendModule(environment: Environment, configuration: Configuration) extends AbstractModule {
  private val jarFiles = new File("extensions/").listFiles.filter(_.getName.endsWith(".jar"))
  private val jarUrls = jarFiles.map(_.toURI.toURL)

  private val backendClasses = jarFiles.flatMap(f => Option(new JarFile(f).getManifest)).flatMap { manifest =>
    Option(manifest.getMainAttributes)
  }.flatMap { mainAttr =>
    Option(mainAttr.getValue("Backend-Class"))
  }

  private val backendClass = configuration.getOptional[String]("cyan.backend.class").orElse(backendClasses.headOption)

  private val beClassDir = configuration.getOptional[String]("cyan.backend.classpath")
  private val dirUrls = beClassDir.map(s => Seq(new File(s).toURI.toURL)).getOrElse(Seq())
  private val lookUrls = jarUrls ++ dirUrls
  Logger.info("Backend classpath URLs: " + lookUrls.mkString(","))
  Logger.info("Backend class: " + backendClass)

  val classloader = new URLClassLoader(lookUrls, environment.classLoader)

  override def configure(): Unit = {

    val backendModuleClass =
      backendClass.flatMap[Class[_ <: Backend]] { cls =>
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

    bind(classOf[Backend]).to(backendModuleClass)
  }
}
