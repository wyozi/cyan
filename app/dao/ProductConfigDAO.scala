package dao

import com.google.inject.{Inject, Singleton}
import model.ProductConfig
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class ProductConfigDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] {


  import driver.api._

  private[dao] val ProductConfigs = TableQuery[ProductConfigTable]

  def getValue(productId: Int, key: String): Future[Option[String]] =
    db.run(ProductConfigs.filter(pc => pc.prodId === productId && pc.key === key).map(_.value).result.headOption)

  def upsertValue(productId: Int, key: String, value: String): Future[Unit] = {
    db.run(
      ProductConfigs
        .insertOrUpdate(ProductConfig(productId, key, value))
        .map(_ => ())
    )
  }

  private[dao] class ProductConfigTable(tag: Tag) extends Table[ProductConfig](tag, "product_config") {
    def prodId = column[Int]("product_id", O.PrimaryKey)
    def key = column[String]("key", O.SqlType("VARCHAR(16)"), O.PrimaryKey)
    def value = column[String]("value")

    override def * = (prodId, key, value) <> ((ProductConfig.apply _).tupled, ProductConfig.unapply)
  }
}
