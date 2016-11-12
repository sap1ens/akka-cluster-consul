package com.sap1ens.api

import scala.concurrent.ExecutionContext
import spray.util.LoggingContext
import spray.json.DefaultJsonProtocol
import spray.routing._
import spray.http.StatusCodes
import akka.actor.ActorRef
import akka.pattern.ask

import akka.util.Timeout
import com.sap1ens.KVStorageService.Result

import scala.concurrent.duration._

import scala.util.Success
import scala.util.Failure

object KVRoutes {
  case class TestAPIObject(thing: String)

  object KVRoutesProtocol extends DefaultJsonProtocol {
    implicit val resultFormat = jsonFormat1(Result)
  }
}

class KVRoutes(kvService: () => ActorRef)(implicit ec: ExecutionContext, log: LoggingContext) extends ApiRoute {

  import KVRoutes._
  import KVRoutesProtocol._
  import com.sap1ens.api.ApiRoute._
  import com.sap1ens.KVStorageService._
  import ApiRouteProtocol._

  implicit val timeout = Timeout(10 seconds)

  val route: Route =
    path("kv" / Segment) { (key) =>
      get {
        val future = (kvService() ? Get(key)).mapTo[KVResult]

        onComplete(future) {
          case Success(result @ Result(_: Some[String])) =>
            complete(result)

          case Success(Result(None)) =>
            complete(StatusCodes.NotFound)

          case Failure(e) =>
            log.error(s"Error: ${e.toString}")
            complete(StatusCodes.InternalServerError, Message(ApiMessages.UnknownException))
        }
      }
    } ~
    path("kv" / Segment / Segment) { (key, value) =>
      put {
        val future = (kvService() ? Set(key, value)).mapTo[KVResult]

        onComplete(future) {
          case Success(_) =>
            complete(StatusCodes.OK)

          case Failure(e) =>
            log.error(s"Error: ${e.toString}")
            complete(StatusCodes.InternalServerError, Message(ApiMessages.UnknownException))
        }
      }
    } ~
    path("kv" / Segment) { (key) =>
      delete {
        val future = (kvService() ? Delete(key)).mapTo[KVResult]

        onComplete(future) {
          case Success(_) =>
            complete(StatusCodes.OK)

          case Failure(e) =>
            log.error(s"Error: ${e.toString}")
            complete(StatusCodes.InternalServerError, Message(ApiMessages.UnknownException))
        }
      }
    }

}
