package auth

import play.api.mvc._

/**
  * Created by wyozi on 3.2.2016.
  */
trait Secured { self: Results =>

  def isLoggedIn(req: Request[AnyContent]): Boolean = req.session.get("loggedIn").isDefined

  def setLoggedIn(req: Request[AnyContent]): Unit = {
    req.session + ("loggedIn", "true")
  }

  def SecureAction(block: (Request[AnyContent]) => Result): Action[AnyContent] = Action { req =>
    if (isLoggedIn(req)) {
      block(req)
    } else {
      req.headers.get("Authorization").flatMap { authorization =>
        authorization.split(" ").drop(1).headOption.filter { encoded =>
          new String(org.apache.commons.codec.binary.Base64.decodeBase64(encoded.getBytes)).split(":").toList match {
            case u :: p :: Nil if u == "admin" && "admin" == p => true
            case _ => false
          }
        }
      } match {
        case Some(_) => { setLoggedIn(req); block(req) }
        case _ => Unauthorized.withHeaders("WWW-Authenticate" -> """Basic realm="Secured Area"""")
      }
    }
  }
  def SecureAction(block: => Result): Action[AnyContent] = SecureAction(_ => block)
}
