package auth

import com.mohiva.play.silhouette.api.Identity

case class User(githubUser: String) extends Identity