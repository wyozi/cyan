package model

import java.sql.Timestamp

/**
  * Created by wyozi on 3.2.2016.
  */
case class Ping(id: Int, date: Timestamp, product: String, license: String, user: String, ip: String, responseId: Option[Int])