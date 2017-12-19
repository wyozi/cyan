package auth

import com.google.inject.Inject
import play.api.Configuration
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class Authentication @Inject() (val config: Configuration, actionBuilder: DefaultActionBuilder) {
  private lazy val password: Option[String] = config.getOptional[String]("cyan.password")

  private def isAuthorized(req: RequestHeader): Boolean = req.session.get("loggedIn") match {
    case Some("true") => true
    case _ => false
  }

  private def withAuthorization(res: Result): Result = {
    res.withSession(("loggedIn", "true"))
  }

  private def testAuth(testUser: String, testPass: String): Boolean = (Some("admin"), password) match {
    case (Some(user), Some(pass)) if user == testUser && pass == testPass => true
    case _ => false
  }

  def adminOnly[A](action: Action[A])(implicit ec: ExecutionContext): Action[A] = actionBuilder.async(action.parser) { request =>
    if (isAuthorized(request)) {
      action(request)
    } else {
      request.headers.get("Authorization").flatMap { authorization =>
        authorization.split(" ").drop(1).headOption.map { encoded =>
          new String(org.apache.commons.codec.binary.Base64.decodeBase64(encoded.getBytes)).split(":").toList match {
            case u :: p :: Nil if testAuth(u, p) => true
            case _ => false
          }
        }
      } match {
          case Some(true) => action(request).map(res => withAuthorization(res))
          case Some(false) => Future.successful(Results.Unauthorized.withHeaders("WWW-Authenticate" -> """Basic realm="Invalid login; try again.""""))
          case _ => Future.successful(Results.Unauthorized.withHeaders("WWW-Authenticate" -> """Basic realm="Cyan login""""))
      }
    }
  }

  def adminOnly(block: (Request[AnyContent]) => Result)(implicit ec: ExecutionContext, parser: BodyParsers.Default): Action[AnyContent] = adminOnly(actionBuilder { request =>
    block(request)
  })

  def adminOnlyAsync(block: (Request[AnyContent]) => Future[Result])(implicit ec: ExecutionContext, parser: BodyParsers.Default): Action[AnyContent] = adminOnly(actionBuilder.async { request =>
    block(request)
  })
}
