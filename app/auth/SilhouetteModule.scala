package auth

import com.google.inject.{Provides, AbstractModule}
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import com.mohiva.play.silhouette.impl.providers.oauth2.GitHubProvider

/**
  * Created by wyozi on 13.2.2016.
  */
class SilhouetteModule extends AbstractModule {
  override def configure(): Unit = {}

  @Provides
  def provideSocialProviderRegistry(gitHubProvider: GitHubProvider): SocialProviderRegistry = {
    SocialProviderRegistry(Seq(gitHubProvider))
  }
}
