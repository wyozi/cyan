package model

import java.sql.Date

import anorm._
import anorm.SqlParser._
import org.joda.time.DateTime
import play.api.db.DB

/**
  * Created by wyozi on 3.2.2016.
  */
case class Ping(id: Int, date: DateTime, prod: Product, license: String, user: String, responseId: Option[Int]) {
  def response: Option[Response] = responseId.flatMap(id => Response.getById(id))
}
object Ping {
  val productlessParser = {
    for {
      id <- int("id")
      date <- get[DateTime]("date")
      license <- str("license")
      user <- str("user_name")
      responseId <- get[Option[Int]]("response_id")
    } yield {
      Ping(id, date, _: Product, license, user, responseId)
    }
  }

  def productParser(p: Product): RowParser[Ping] = productlessParser.map(_.apply(p))
}