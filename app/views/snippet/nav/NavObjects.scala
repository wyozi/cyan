package views.snippet.nav

import play.api.mvc.Call
import play.twirl.api.Html

/**
  * Contains utilities for parsing navigation objects (eg. for breadcrumbs)
  */
object NavObjects {
  import play.twirl.api.StringInterpolation

  def parse(obj: Any, active: Boolean = false): (Html, Option[Call]) = obj match {
    case prod: model.Product => (html"${prod.name}", Some(controllers.admin.prod.routes.Products.view(prod.id)))
    case resp: model.Response => (html"""${resp.name}""", Some(controllers.admin.routes.Responses.view(resp.id)))
    case (name: String, path: Call) => (html"""$name""", Some(path))
    case (html: Html, path: Call) => (html, Some(path))
    case 'root => (Html("Cyan"), Some(controllers.admin.routes.Main.index()))
    case h: Html => (h, None)
    case x => (html"""$x""", None)
  }

  private val TagRegex = """<(.*?)>""".r

  /**
    * Attempts to parse tags from input string using a simple regexp.
    * Note: stripping is far from perfect, but safe htmlescaping is not needed here because
    *       output will be escaped anyway. This method exists to make the output more aesthetic.
    */
  private def stripTags(str: String) = TagRegex.replaceAllIn(str, "")

  /**
    * Parse a single part in navigation path to a string.
    *
    * @param obj
    * @return
    */
  def parseToString(obj: Any): String = stripTags(parse(obj)._1.toString())

  /**
    * Parse rthe whole navigation path into a string
    * @param seq
    * @return
    */
  def mkString(seq: Seq[Any]) = seq.map(parseToString(_)).mkString(" > ")
}
