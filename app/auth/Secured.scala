package auth

import play.api.mvc._

import scala.concurrent.Future

/**
  * Created by wyozi on 3.2.2016.
  */
trait Secured { self: Results =>

  def isLoggedIn(req: Request[_]): Boolean = req.session.get("loggedIn").isDefined

  def setLoggedIn(req: Request[_]): Unit = {
    req.session + ("loggedIn", "true")
  }

  object SecureAction extends ActionBuilder[Request] {
    override def invokeBlock[A](req: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
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
          case _ => Future.successful(Unauthorized.withHeaders("WWW-Authenticate" -> """Basic realm="Secured Area""""))
        }
      }
    }
  }
}
