package model

/**
  * Created by wyozi on 3.2.2016.
  */
case class Response(id: Int, name: String, body: String, color: NotificationColor.NotificationColor) {
  def bootstrap3ClassSuffix: String = color match {
    // TODO come up with colors for these
    case NotificationColor.light | NotificationColor.dark | NotificationColor.secondary => "default"
    case x => x.toString
  }
}