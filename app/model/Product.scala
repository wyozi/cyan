package model

import anorm._
import play.api.db.DB

/**
  * Created by wyozi on 3.2.2016.
  */
case class Product(id: Int, name: String, shortName: String) {
  import play.api.Play.current

  def getRecentPings: List[Ping] = {
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM Pings WHERE product = {shortName} ORDER BY date DESC")
        .on('shortName -> shortName)
        .as(Ping.productParser(this).*)
    }
  }

  def getLicense(license: String): ProductLicense = ProductLicense(this, license)
}
object Product {
  import play.api.Play.current

  import anorm._
  import anorm.SqlParser._
  val Parser = for {
    id <- int("id")
    name <- str("name")
    shortName <- str("shortName")
  } yield {
    Product(id, name, shortName)
  }

  def getAll: List[Product] = {
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM Products").as(Parser.*)
    }
  }

  def getId(id: Int): Option[Product] = {
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM Products WHERE id = {id}").on('id -> id).as(Parser.singleOpt)
    }
  }
  def insert(name: String, shortName: String) = {
    DB.withConnection { implicit c =>
      SQL("INSERT INTO Products(name, shortName) VALUES({name}, {shortName})")
        .on('name -> name, 'shortName -> shortName)
        .executeInsert()
    }
  }
}