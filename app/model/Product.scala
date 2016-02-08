package model

import anorm._
import play.api.db.DB

/**
  * Created by wyozi on 3.2.2016.
  */
case class Product(id: Int, name: String, shortName: String)