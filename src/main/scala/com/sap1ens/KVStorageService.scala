package com.sap1ens

import akka.actor.{Actor, ActorLogging, Props}

object KVStorageService {
  sealed trait KVCommand
  case class Get(key: String) extends KVCommand
  case class Set(key: String, value: String) extends KVCommand
  case class Delete(key: String) extends KVCommand

  sealed trait KVResult
  case class Result(value: Option[String]) extends KVResult
  case class Updated(key: String) extends KVResult
  case class Deleted(key: String) extends KVResult

  def props() = Props(classOf[KVStorageService])
}

class KVStorageService extends Actor with ActorLogging {
  import KVStorageService._

  private var data: Map[String, String] = Map.empty

  def receive = {
    case Get(key) => {
      log.info(s"Looking for key: $key")

      sender() ! Result(data.get(key))
    }

    case Set(key, value) => {
      log.info(s"Updating key: $key with value: $value")

      data = data.updated(key, value)

      sender() ! Updated(key)
    }

    case Delete(key) => {
      log.info(s"Deleting key: $key")

      data = data - key

      sender() ! Deleted(key)
    }
  }
}
