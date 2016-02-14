package controllers.auth

import auth.{User, UserService}
import com.google.inject.Inject
import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import play.api.i18n.MessagesApi

/**
  * Created by wyozi on 13.2.2016.
  */
class SocialAuth @Inject() (
  val messagesApi: MessagesApi,
  val env: Environment[User, CookieAuthenticator],
  userService: UserService,
  socialProviderRegistry: SocialProviderRegistry) extends Silhouette[User, CookieAuthenticator] {
/*

  def authenticateGithub() = Action.async { implicit request =>
    socialProviderRegistry.get[SocialProvider]("github") match {
      case Some(p: SocialProvider with CommonSocialProfileBuilder) =>
        p.authenticate().flatMap {
          case Left(result) => Future.successful(result)
          case Right(authInfo) => for {
            profile <- p.retrieveProfile(authInfo)
            user <- userService.save(profile)
            authenticator <- env.authenticatorService.create(profile.loginInfo)
            value <- env.authenticatorService.init(authenticator)
            result <- env.authenticatorService.embed(value, Redirect(routes.ApplicationController.index()))
          } yield {
            env.eventBus.publish(LoginEvent(user, request, request2Messages))
            result
          }
        }
      case _ => Future.failed(new RuntimeException("WTF"))
    }
  }
 */
}
