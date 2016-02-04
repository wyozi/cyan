package model

import anorm._
import play.api.db.DB

/**
  * Created by wyozi on 3.2.2016.
  */
case class Product(id: Int, name: String, shortName: String, var defaultUnregResponse: Option[Int] = None, var defaultRegResponse: Option[Int] = None) {
  import play.api.Play.current

  def getRecentPings: List[Ping] = {
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM Pings WHERE product = {shortName} ORDER BY date DESC")
        .on('shortName -> shortName)
        .as(Ping.productParser(this).*)
    }
  }

  def getPingCount: Int = {
    DB.withConnection { implicit c =>
      SQL("SELECT COUNT(*) c FROM Pings WHERE product = {shortName}")
        .on('shortName -> shortName)
        .as(SqlParser.int("c").single)
    }
  }

  def getLicense(license: String): ProductLicense = ProductLicense(this, license)

  def updateDefaultUnregResponse(responseId: Option[Int]): Unit = {
    DB.withConnection { implicit c =>
      SQL("UPDATE Products SET defaultresp_unreg = {responseId} WHERE id = {id}")
        .on('id -> id, 'responseId -> responseId)
        .executeUpdate()
    }
    defaultUnregResponse = responseId
  }
  def updateDefaultRegResponse(responseId: Option[Int]): Unit = {
    DB.withConnection { implicit c =>
      SQL("UPDATE Products SET defaultresp_reg = {responseId} WHERE id = {id}")
        .on('id -> id, 'responseId -> responseId)
        .executeUpdate()
    }
    defaultRegResponse = responseId
  }
}
object Product {
  import play.api.Play.current

  import anorm._
  import anorm.SqlParser._
  val Parser = for {
    id <- int("id")
    name <- str("name")
    shortName <- str("shortName")

    defUnreg <- get[Option[Int]]("defaultresp_unreg")
    defReg <- get[Option[Int]]("defaultresp_reg")
  } yield {
    Product(id, name, shortName, defUnreg, defReg)
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