package auth

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile

import scala.concurrent.Future

class UserService extends IdentityService[User] {
  val users = collection.mutable.Buffer[User]()

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    Future.successful(users.find(_.githubUser == loginInfo.providerID))
  }

  def save(user: User): Unit = {
    users += user
  }

  def save(profile: CommonSocialProfile): Unit = {
    save(User(profile.email.get))
  }
}