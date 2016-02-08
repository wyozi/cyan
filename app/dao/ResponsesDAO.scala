package dao

import com.google.inject.Inject
import model.Response
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by wyozi on 8.2.2016.
  */
class ResponsesDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  private val Responses = TableQuery[ResponsesTable]

  def getAll(): Future[Seq[Response]] =
    db.run(Responses.result)

  def insert(prod: Response): Future[Unit] =
    db.run(Responses += prod).map(_ => ())

  def findById(id: Int): Future[Option[Response]] =
    db.run(Responses.filter(_.id === id).result.headOption)

  private class ResponsesTable(tag: Tag) extends Table[Response](tag, "RESPONSES") {
    def id = column[Int]("ID", O.AutoInc)

    def name = column[String]("NAME", O.SqlType("VARCHAR(64)"))
    def body = column[String]("RESPONSE")

    override def * = (id, name, body) <> (Response.tupled, Response.unapply)
  }
}

