package controllers.admin

import javax.inject.Inject

import auth.Authentication
import dao.PingExtrasDAO
import play.api.mvc.{AbstractController, BodyParsers, ControllerComponents}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by wyozi on 6.2.2016.
  */
class Pings @Inject()
  (cc: ControllerComponents,
   auth: Authentication)
  (implicit pingExtrasDAO: PingExtrasDAO, parser: BodyParsers.Default, ec: ExecutionContext) extends AbstractController(cc) {

  def showPingExtra(pingId: Int, extraKey: String) = auth.adminOnlyAsync { req =>
    pingExtrasDAO.findValue(pingId, extraKey).map {
      case Some(value) => Ok(value)
      case _ => NotFound("")
    }
  }
}
