package backend

import java.io.File
import java.net.URLClassLoader

import com.google.inject.AbstractModule
import cyan.backend.Backend
import cyan.backend.impl.DefaultBackend
import play.api.{Configuration, Environment}

/**
  * Created by wyozi on 6.2.2016.
  */
class BackendModule(environment: Environment, configuration: Configuration) extends AbstractModule {
  override def configure(): Unit = {
    val be = configuration.getString("cyan.backend")


    val backendClass = be match {
      case Some(cls) =>
        val jarUrls = new File("extensions/").listFiles.filter(_.getName.endsWith(".jar")).map(_.toURI.toURL)
        val jarCl = new URLClassLoader(jarUrls, environment.classLoader)

        jarCl.loadClass(cls).asSubclass(classOf[Backend])
      case _ => classOf[DefaultBackend]
    }

    bind(classOf[Backend]).to(backendClass)
  }
}
