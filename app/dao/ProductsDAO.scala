package dao

import com.google.inject.{Singleton, Inject}
import model.Product
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

/**
  * Created by wyozi on 8.2.2016.
  */
@Singleton
class ProductsDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private[dao] val Products = TableQuery[ProductsTable]

  def getAll(): Future[Seq[Product]] =
    db.run(Products.result)

  def insert(name: String, shortName: String): Future[Int] =
    db.run(Products.map(c => (c.name, c.shortName)) returning Products.map(_.id) += (name, shortName))

  def findById(id: Int): Future[Option[Product]] =
    db.run(Products.filter(_.id === id).result.headOption)

  def findByShortName(shortName: String): Future[Option[Product]] =
    db.run(Products.filter(_.shortName === shortName).result.headOption)

  def findByShortNames(shortNames: Seq[String]): Future[Seq[Product]] =
    db.run(Products.filter(_.shortName inSetBind shortNames).result)


  private[dao] class ProductsTable(tag: Tag) extends Table[Product](tag, "products") {
    def id = column[Int]("id", O.AutoInc)

    def name = column[String]("name", O.SqlType("VARCHAR(255)"))
    def shortName = column[String]("short_name", O.SqlType("VARCHAR(16)"))

    override def * = (id, name, shortName) <> (Product.tupled, Product.unapply)
  }
}
