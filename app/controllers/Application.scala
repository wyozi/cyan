package controllers

import play.api.mvc._

class Application extends Controller {

  def index = Action {
    Status(418)
  }

}