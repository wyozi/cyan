package controllers.admin

import javax.inject.Inject

import auth.Secured
import dbrepo.PingExtrasRepository
import play.api.mvc.Controller

/**
  * Created by wyozi on 6.2.2016.
  */
class Pings @Inject() (implicit pingExtrasRepo: PingExtrasRepository) extends Controller with Secured {
  def showPingExtra(pingId: Int, extraKey: String) = SecureAction { req =>
    pingExtrasRepo.getExtraValue(pingId, extraKey) match {
      case Some(value) => Ok(value)
      case _ => NotFound("")
    }
  }
}
