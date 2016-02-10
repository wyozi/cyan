package dao

import com.google.inject.Inject
import model.Response
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
  * Created by wyozi on 8.2.2016.
  */
class ResponsesDAO @Inject() ()(protected implicit val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  private[dao] val Responses = TableQuery[ResponsesTable]

  def getAll(): Future[Seq[Response]] =
    db.run(Responses.result)

  def insert(name: String, body: String): Future[Int] =
    db.run((Responses.map(c => (c.name, c.body)) returning Responses.map(_.id)) += (name, body))

  def findById(id: Int): Future[Option[Response]] =
    db.run(Responses.filter(_.id === id).result.headOption)

  private[dao] class ResponsesTable(tag: Tag) extends Table[Response](tag, "responses") {
    def id = column[Int]("id", O.AutoInc)

    def name = column[String]("name", O.SqlType("VARCHAR(64)"))
    def body = column[String]("response")

    override def * = (id, name, body) <> (Response.tupled, Response.unapply)
  }
}

