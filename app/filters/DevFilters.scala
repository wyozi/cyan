package filters

import javax.inject.Inject
import filters.impl.LoggingFilter
import play.api.http.HttpFilters

class DevFilters @Inject() (
  log: LoggingFilter
) extends HttpFilters {

  val filters = Seq(log)
}