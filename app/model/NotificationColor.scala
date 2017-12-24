package model

/**
  * Represents the 'color' of an object. Color in this context means the type.
  * Maps directly to the 'notification_color' SQL type.
  */
object NotificationColor extends Enumeration {
  type NotificationColor = Value
  val primary, secondary, success, danger, warning, info,light, dark = Value
}
