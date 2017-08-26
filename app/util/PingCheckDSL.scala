package util

import javax.inject.Inject

import dao.ProductConfigDAO
import model.{Product, ProductConfig, Response}
import org.luaj.vm2._
import org.luaj.vm2.lib.TableLib

import scala.concurrent.{ExecutionContext, Future}

/**
  * Compiler/invoker for ping verification DSL
  * Contains an internal script cache so should be injected to each usepoint
  */
class PingCheckDSL @Inject()(implicit val productConfigDAO: ProductConfigDAO,
  ec: ExecutionContext) {

  private def getPingCheckChunk(prod: Product): Future[Option[LuaValue]] = {
    productConfigDAO.getValue(prod.id, ProductConfig.Keys.PingCheckScript).map {
      case Some(code) if code.trim.nonEmpty =>
        import org.luaj.vm2.LoadState
        import org.luaj.vm2.compiler.LuaC
        import org.luaj.vm2.lib.PackageLib
        import org.luaj.vm2.lib.StringLib
        import org.luaj.vm2.lib.jse.JseBaseLib
        import org.luaj.vm2.lib.jse.JseMathLib

        val server_globals = new Globals
        server_globals.load(new JseBaseLib)
        server_globals.load(new PackageLib)
        server_globals.load(new StringLib)
        server_globals.load(new TableLib)

        // To load scripts, we occasionally need a math library in addition to compiler support.
        // To limit scripts using the debug library, they must be closures, so we only install LuaC.
        server_globals.load(new JseMathLib)
        LoadState.install(server_globals)
        LuaC.install(server_globals)

        Some(server_globals.load(code))
      case _ => None
    }
  }

  /**
    * Last-minute ping registration checks.
    * Runs custom ping check DSLs etc with already valid Product, License etc..
    * Returns Ok[Some[String]] if response should be modified
    * Returns Ok[None] if ping OK but response kept the same
    * Returns Failure if ping addition should fail
    */
  def check(prod: Product, license: String, user: String, extras: Map[String, String], response: Option[Response]): Future[Option[String]] = {
    getPingCheckChunk(prod).flatMap {
      case Some(chunk) =>
        try {
          val ret = chunk.invoke(LuaValue.varargsOf(Array(
            LuaString.valueOf(prod.shortName),
            LuaString.valueOf(license),
            LuaString.valueOf(user),
            LuaValue.tableOf(extras.toList.flatMap (x => List(x._1, x._2)).map(x => LuaValue.valueOf(x)).toArray)
          )))
          println(s"ret0: ${ret.arg1()}")

          val farg = ret.arg1()
          if (farg.isboolean() && farg.checkboolean()) {
            Future.successful(None)
          } else {
            Future.failed(new Exception)
          }
        } catch {
          case e: LuaError => Future.failed(e)
        }
      case _ => Future.successful(None) // No compilable ping check code
    }
  }
}
