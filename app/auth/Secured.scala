package auth

import javax.inject.Inject

import play.api.Play
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by wyozi on 3.2.2016.
  */
trait Secured {

  def isAuthorized(req: RequestHeader): Boolean = req.session.get("loggedIn") match {
    case Some("true") => true
    case _ => false
  }

  private def withAuthorization(res: Result): Result = {
    res.withSession(("loggedIn", "true"))
  }

  lazy val password = Play.current.configuration.getString("cyan.password")

  private def testAuth(testUser: String, testPass: String): Boolean = (Some("admin"), password) match {
    case (Some(user), Some(pass)) if user == testUser && pass == testPass => true
    case _ => false
  }

  class SecureAction @Inject() (implicit ec: ExecutionContext, parser: BodyParsers.Default) extends ActionBuilderImpl(parser) {
    override def invokeBlock[A](req: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
      if (isAuthorized(req)) {
        block(req)
      } else {
        req.headers.get("Authorization").flatMap { authorization =>
          authorization.split(" ").drop(1).headOption.filter { encoded =>
            new String(org.apache.commons.codec.binary.Base64.decodeBase64(encoded.getBytes)).split(":").toList match {
              case u :: p :: Nil if testAuth(u, p) => true
              case _ => false
            }
          }
        } match {
          case Some(_) => block(req).map(res => withAuthorization(res))
          case _ => Future.successful(Results.Unauthorized.withHeaders("WWW-Authenticate" -> """Basic realm="Secured Area""""))
        }
      }
    }
  }
  object SecureAction {
    def apply(block: (Request[AnyContent]) => Result)(implicit ec: ExecutionContext, parser: BodyParsers.Default) = new SecureAction().apply(block)
    def apply(block:  => Result)(implicit ec: ExecutionContext, parser: BodyParsers.Default) = new SecureAction().apply(_ => block)

    def async(block: (Request[AnyContent]) => Future[Result])(implicit ec: ExecutionContext, parser: BodyParsers.Default) = new SecureAction().async(block)
    def async(block: => Future[Result])(implicit ec: ExecutionContext, parser: BodyParsers.Default) = new SecureAction().async(_ => block)
  }

}
