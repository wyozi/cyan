package dao

import java.sql.Timestamp

import com.google.inject.{Inject, Singleton}
import model.NotificationColor.NotificationColor
import model.{NotificationColor, Response}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by wyozi on 8.2.2016.
  */
@Singleton
class ResponsesDAO @Inject() ()(protected implicit val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._
  import ResponsesTable._

  private[dao] val Responses = TableQuery[ResponsesTable]

  def getAll(): Future[Seq[Response]] =
    db.run(Responses.result)

  def insert(name: String, body: String): Future[Int] =
    db.run((Responses.map(c => (c.name, c.body)) returning Responses.map(_.id)) += (name, body))

  def updateName(respId: Int, name: String)(implicit ec: ExecutionContext): Future[Unit] =
    db.run(Responses.filter(_.id === respId).map(_.name).update(name)).map(_ => ())

  def updateBody(respId: Int, body: String)(implicit ec: ExecutionContext): Future[Unit] =
    db.run(Responses.filter(_.id === respId).map(_.body).update(body)).map(_ => ())

  def updateColor(respId: Int, color: NotificationColor.NotificationColor)(implicit ec: ExecutionContext): Future[Unit] =
    db.run(
      sqlu"""
            UPDATE Responses
            SET color = ${color.toString}::notification_color
            WHERE id = $respId
      """.map(_ => ())
    )

  def findById(id: Int): Future[Option[Response]] =
    db.run(Responses.filter(_.id === id).result.headOption)

  def findByIds(ids: Seq[Int]): Future[Seq[Response]] =
    db.run(Responses.filter(_.id inSetBind ids).result)


  private[dao] class ResponsesTable(tag: Tag) extends Table[Response](tag, "responses") {
    import ResponsesTable._

    def id = column[Int]("id", O.AutoInc)

    def name = column[String]("name", O.SqlType("VARCHAR(64)"))
    def body = column[String]("response")
    def color = column[NotificationColor.NotificationColor]("color")

    override def * = (id, name, body, color) <> (Response.tupled, Response.unapply)
  }
  object ResponsesTable {
    implicit val colorColumnType: BaseColumnType[NotificationColor] = MappedColumnType.base[NotificationColor.NotificationColor, String](
      { nc => nc.toString },
      { s => NotificationColor.withName(s)}
    )
  }
}