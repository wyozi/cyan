package model

import java.sql.Date

import anorm._
import anorm.SqlParser._
import org.joda.time.DateTime
import play.api.db.DB

/**
  * Created by wyozi on 3.2.2016.
  */
case class Ping(id: Int, date: DateTime, prod: Product, license: String, user: String) {
  import play.api.Play.current

  def getResponseId: Option[Int] = {
    DB.withConnection { implicit c =>
      SQL("SELECT response FROM PingResponses " +
          "WHERE response IS NOT NULL AND userId = {user} AND licenseId = {license} AND productId = {prod}")
          .on('user -> user)
          .on('license -> license)
          .on('prod -> prod.id)
          .as(int("response").singleOpt)
    }
  }
  def getResponse: Option[Response] = getResponseId.flatMap(id => Response.getId(id))
}
object Ping {
  val productlessParser = {
    for {
      id <- int("id")
      date <- get[DateTime]("date")
      license <- str("licenseId")
      user <- str("userId")
    } yield {
      Ping(id, date, _: Product, license, user)
    }
  }

  def productParser(p: Product): RowParser[Ping] = productlessParser.map(_.apply(p))
}