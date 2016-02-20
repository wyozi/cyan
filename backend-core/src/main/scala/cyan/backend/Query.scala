package cyan.backend

import cyan.backend.model.{BackendLicense, BackendProduct}

case class Query(id: String, params: Map[String, String], product: Option[BackendProduct] = None, license: Option[BackendLicense] = None)
