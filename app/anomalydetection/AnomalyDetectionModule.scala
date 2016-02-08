package anomalydetection

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import anomalydetection.impl.{ManyUsersOneLicense, ExcessivePings}

/**
  * Created by wyozi on 8.2.2016.
  */
class AnomalyDetectionModule extends AbstractModule {
  override def configure(): Unit = {
    val anomalyDetectorBinder = Multibinder.newSetBinder(binder(), classOf[AnomalyDetector])
    anomalyDetectorBinder.addBinding().to(classOf[ExcessivePings])
    anomalyDetectorBinder.addBinding().to(classOf[ManyUsersOneLicense])
  }
}
