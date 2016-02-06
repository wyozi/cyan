package cyan.backend

import play.twirl.api.Html

/**
  * Created by wyozi on 6.2.2016.
  */
trait Backend {
  /**
    *  Returns additional user-specific html content that is shown in a pop-up upon clicking a button next to
    *  the user element.
    *
    *  NOTE: Make sure you don't introduce XSS- bugs if you include `user` in the returned HTML.
    *
    * @param user
    * @return
    */
  def createUserPopupHTML(user: String): Option[Html]

  /**
    *  Returns additional license-specific html content that is shown in a pop-up upon clicking a button next to
    *  the license element.
    *
    *  NOTE: Make sure you don't introduce XSS- bugs if you include `user` in the returned HTML.
    *
    * @param license
    * @return
    */
  def createLicensePopupHTML(license: String): Option[Html]
}
