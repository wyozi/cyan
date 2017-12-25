package cyan.util

import play.twirl.api._

/**
  * Helpers to make working with Html objects easier
  */
object TwirlHelpers {
  implicit class HtmlExt(html: Html) {
    def +(other: Html): Html = {
      new BaseScalaTemplate[Html, Format[Html]](HtmlFormat)._display_(Array(html, other))
    }
  }
}
