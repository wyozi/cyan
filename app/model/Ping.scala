package model

import java.sql.Date

/**
  * Created by wyozi on 3.2.2016.
  */
case class Ping(id: Int, date: Date, product: String, license: String, user: String, ip: String, responseId: Option[Int])