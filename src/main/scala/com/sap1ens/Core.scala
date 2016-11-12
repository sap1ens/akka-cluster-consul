package com.sap1ens

import akka.actor.{ActorRef, ActorSystem, Cancellable}
import akka.cluster.Cluster
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings, ClusterSingletonProxy, ClusterSingletonProxySettings}
import com.sap1ens.utils.ConfigHolder
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

trait Core {
  implicit def system: ActorSystem
}

trait BootedCore extends Core {
  implicit lazy val system = ActorSystem("microservice-system")

  sys.addShutdownHook(system.terminate())
}

trait CoreActors extends ConfigHolder {
  this: Core =>
}

trait ClusteredBootedCore extends BootedCore with CoreActors with LazyLogging {

  var kvService: ActorRef = _
  val getKVService = () => kvService

  /**
    * Creating Cluster Singleton for KVStorageService
    */
  def init() = {
    system.actorOf(ClusterSingletonManager.props(
      singletonProps = KVStorageService.props(),
      terminationMessage = KVStorageService.End,
      settings = ClusterSingletonManagerSettings(system)),
      name = "kvService")

    kvService = system.actorOf(ClusterSingletonProxy.props(
      singletonManagerPath = s"/user/kvService",
      settings = ClusterSingletonProxySettings(system)),
      name = "kvServiceProxy")
  }

  val cluster = Cluster(system)
  val isConsulEnabled = config.getBoolean("consul.enabled")

  // retrying cluster join until success
  val scheduler: Cancellable = system.scheduler.schedule(10 seconds, 30 seconds, new Runnable {
    override def run(): Unit = {
      val selfAddress = cluster.selfAddress
      logger.debug(s"Cluster bootstrap, self address: $selfAddress")

      val serviceSeeds = if (isConsulEnabled) {
        val serviceAddresses = ConsulAPI.getServiceAddresses
        logger.debug(s"Cluster bootstrap, service addresses: $serviceAddresses")

        // http://doc.akka.io/docs/akka/2.4.4/scala/cluster-usage.html
        //
        // When using joinSeedNodes you should not include the node itself except for the node
        // that is supposed to be the first seed node, and that should be placed first
        // in parameter to joinSeedNodes.
        serviceAddresses filter { address =>
          address != selfAddress || address == serviceAddresses.head
        }
      } else {
        List(selfAddress)
      }

      logger.debug(s"Cluster bootstrap, found service seeds: $serviceSeeds")

      cluster.joinSeedNodes(serviceSeeds)
    }
  })

  cluster registerOnMemberUp {
    logger.info("Cluster is ready!")

    scheduler.cancel()

    init()
  }
}