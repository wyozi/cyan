package model

import play.api.db.DB

/**
  * Created by wyozi on 3.2.2016.
  */
case class Response(id: Int, name: String, body: String)
object Response {
  import play.api.Play.current
  import anorm._
  import anorm.SqlParser._

  val Parser = for {
    id <- int("id")
    name <- str("name")
    response <- str("response")
  } yield {
    Response(id, name, response)
  }

  def getAll: List[Response] = {
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM Responses").as(Parser.*)
    }
  }
  def getById(id: Int): Option[Response] = {
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM Responses WHERE id = {id}").on('id -> id).as(Parser.singleOpt)
    }
  }
}