package response.impl

import javax.inject.Inject

import anorm.SqlParser._
import anorm._
import dbrepo.PingResponseRepository
import model.Response
import play.api.db.DB
import response.{ResponseFindParameters, ResponseFinder}

/**
  * Created by wyozi on 5.2.2016.
  */
class DatabaseResponseFinder @Inject() (pingRepo: PingResponseRepository) extends ResponseFinder {
  /**
    * Finds suitable response to given parameters.
    */
  override def find(params: ResponseFindParameters): Option[Response] = {
    pingRepo.getBestResponse(params.productId, params.license, params.user)
  }
}
