package dbrepo

import anorm._
import anorm.SqlParser._
import model.Response
import play.api.db.DB

/**
  * Created by wyozi on 5.2.2016.
  */
class ProductRepository {
  def getDefaultUnregisteredResponse(id: Int): Option[Int] = {
    import play.api.Play.current
    DB.withConnection { implicit connection =>
      SQL("SELECT defaultresp_unreg FROM Products WHERE id = {id}")
        .as(int("defaultresp_unreg").singleOpt)
    }
  }
}
