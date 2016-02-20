package cyan.backend

import cyan.backend.model.{BackendLicense, BackendProduct}
import play.twirl.api.Html

import scala.concurrent.Future

/**
  * Created by wyozi on 6.2.2016.
  */
abstract class Backend {

  /**
    *  Allows modifying the html used for product cell in eg. ping tables. Return `content` if you do not
    *  wish to modify the html.
    *
    *  NOTE: Make sure you don't introduce XSS- bugs if you include `license` in the returned HTML.
    *
    * @param product the product
    * @param original original html
    * @return
    */
  def transformProductHtml(product: BackendProduct)(original: Html): Html = original

  /**
    *  Allows modifying the html used for license cell in eg. ping tables. Return `content` if you do not
    *  wish to modify the html.
    *
    *  NOTE: Make sure you don't introduce XSS- bugs if you include `license` in the returned HTML.
    *
    * @param license the license
    * @param original original html
    * @return
    */
  def transformLicenseHtml(license: BackendLicense)(original: Html): Html = original

  /**
    *  Allows modifying the html used for user cell in eg. ping tables. Return `content` if you do not
    *  wish to modify the html.
    *
    *  NOTE: Make sure you don't introduce XSS- bugs if you include `user` in the returned HTML.
    *
    * @param user the user name/id
    * @param original original html
    * @return
    */
  def transformUserHtml(user: String)(original: Html): Html = original

  /**
    * Returns list of custom project configurations. They are basically configuration options
    * that the user can set in each project. This could be for example an API key to query
    * some other backend license server for license validity.
    *
    * @return
    */
  def getCustomProjectConfigs: Seq[CustomProjectConfig] = Nil

  /**
    * Allows backend to reply to a query usually sent by ajax request from html injected by the backend itself.
    * This allows asynchronously loading some data that is not important or is behind another network layer.
    *
    * @param query query object
    * @return
    */
  def respondToQuery(query: Query): Future[Option[Html]] = Future.successful(None)
}
