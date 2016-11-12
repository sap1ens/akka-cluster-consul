package com.sap1ens.api

import spray.http.StatusCodes
import spray.routing._

class HealthCheckRoutes extends ApiRoute {

  val route: Route =
    path("health") {
      get {
        complete(StatusCodes.OK)
      }
    }
}
