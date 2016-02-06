package model

import anorm.SqlParser._
import anorm._
import dbrepo.PingExtrasRepository
import org.joda.time.DateTime

/**
  * Created by wyozi on 3.2.2016.
  */
case class Ping(id: Int, date: DateTime, prod: Product, license: String, user: String, ip: String, responseId: Option[Int]) {
  def response: Option[Response] = responseId.flatMap(id => Response.getById(id))

  def extras()(implicit pingExtrasRepo: PingExtrasRepository) = pingExtrasRepo.getExtrasFor(id)
}
object Ping {
  val productlessParser = {
    for {
      id <- int("id")
      date <- get[DateTime]("date")
      license <- str("license")
      user <- str("user_name")
      ip <- str("ip")
      responseId <- get[Option[Int]]("response_id")
    } yield {
      Ping(id, date, _: Product, license, user, ip, responseId)
    }
  }

  def productParser(p: Product): RowParser[Ping] = productlessParser.map(_.apply(p))
}