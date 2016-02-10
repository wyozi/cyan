package cyan

import org.specs2.execute.{AsResult, Result}
import play.api.db.DBApi
import play.api.db.slick.DatabaseConfigProvider
import play.api.test.{FakeApplication, Helpers, WithApplication}
import slick.backend.DatabaseConfig
import slick.profile.BasicProfile

/**
  * Created by wyozi on 10.2.2016.
  */
class DBApplication extends WithApplication(FakeApplication(additionalConfiguration = Helpers.inMemoryDatabase())) {
  protected implicit def dbConfigProvider = new DatabaseConfigProvider {
    override def get[P <: BasicProfile]: DatabaseConfig[P] = DatabaseConfigProvider.get("default")
  }

  override def around[T](t: => T)(implicit evidence$2: AsResult[T]): Result = {
    val db = implicitApp.injector.instanceOf[DBApi].database("default")

    //Evolutions.applyEvolutions(db)
    try {
      super.around(t)
    } finally {
      //Evolutions.cleanupEvolutions(db)
    }
  }
}
