package response

import com.google.inject.ImplementedBy
import model.Response
import response.impl.DatabaseResponseFinder

/**
  * Created by wyozi on 5.2.2016.
  */
@ImplementedBy(classOf[DatabaseResponseFinder])
trait ResponseFinder {
  /**
    * Finds suitable response to given parameters.
    */
  def find(params: ResponseFindParameters): Option[Response]
}