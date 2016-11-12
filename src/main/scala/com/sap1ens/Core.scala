package com.sap1ens

import akka.actor.ActorSystem
import com.sap1ens.utils.ConfigHolder

trait Core {
  implicit def system: ActorSystem
}

trait BootedCore extends Core {
  implicit lazy val system = ActorSystem("microservice-system")

  sys.addShutdownHook(system.terminate())
}

trait CoreActors extends ConfigHolder {
  this: Core =>

  val kvService = system.actorOf(KVStorageService.props(), "KVStorageService")
}
