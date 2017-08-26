package controllers.admin

import javax.inject.Inject

import auth.Secured
import dao.PingExtrasDAO
import play.api.mvc.{BodyParsers, Controller}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by wyozi on 6.2.2016.
  */
class Pings @Inject() (implicit pingExtrasDAO: PingExtrasDAO, parser: BodyParsers.Default, ec: ExecutionContext) extends Controller with Secured {
  def showPingExtra(pingId: Int, extraKey: String) = SecureAction.async { req =>
    pingExtrasDAO.findValue(pingId, extraKey).map {
      case Some(value) => Ok(value)
      case _ => NotFound("")
    }
  }
}
