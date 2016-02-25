package anomalydetection

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import anomalydetection.impl.{ManyLicensesOneUser, ManyUsersOneLicense}

/**
  * Created by wyozi on 8.2.2016.
  */
class AnomalyDetectionModule extends AbstractModule {
  override def configure(): Unit = {
    val anomalyDetectorBinder = Multibinder.newSetBinder(binder(), classOf[AnomalyDetector])
    anomalyDetectorBinder.addBinding().to(classOf[ManyUsersOneLicense])
    anomalyDetectorBinder.addBinding().to(classOf[ManyLicensesOneUser])
  }
}
