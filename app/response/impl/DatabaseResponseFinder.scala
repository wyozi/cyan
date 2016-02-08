package response.impl

import javax.inject.Inject

import dao.PingResponsesDAO
import model.Response
import response.{ResponseFindParameters, ResponseFinder}

/**
  * Created by wyozi on 5.2.2016.
  */
class DatabaseResponseFinder @Inject() (pingResponsesDAO: PingResponsesDAO) extends ResponseFinder {
  /**
    * Finds suitable response to given parameters.
    */
  override def find(params: ResponseFindParameters): Option[Response] = {
    pingResponsesDAO.getBestResponse(params.productId, params.license, params.user)
  }
}
