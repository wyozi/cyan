package dbrepo

import javax.inject.{Inject, Singleton}

import anorm.SqlParser._
import anorm._
import play.api.db.Database

/**
  * Created by wyozi on 6.2.2016.
  */
@Singleton
class PingExtrasRepository @Inject() (db: Database) {
  def getExtraValue(pingId: Int, key: String): Option[String] = {
    db.withConnection { implicit connection =>
      SQL(
        """
          |SELECT value
          |FROM PingExtras
          |WHERE ping_id = {pingId} AND key = {key}
        """.stripMargin)
        .on('pingId -> pingId, 'key -> key)
        .as(str("value").singleOpt)
    }
  }

  def getExtrasFor(pingId: Int): Map[String, String] = {
    db.withConnection { implicit connection =>
      SQL(
        """
          |SELECT key, value
          |FROM PingExtras
          |WHERE ping_id = {pingId}
        """.stripMargin)
        .on('pingId -> pingId)
        .as(str("key")~str("value")*)
        .map { case key~value => (key, value) }
        .toMap
    }
  }
}
