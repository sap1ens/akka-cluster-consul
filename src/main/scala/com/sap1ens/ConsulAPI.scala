package com.sap1ens

import akka.actor.{ActorSystem, Address}
import com.orbitz.consul.option.{ConsistencyMode, ImmutableQueryOptions}
import com.sap1ens.utils.ConfigHolder

import scala.collection.JavaConversions._

object ConsulAPI extends ConfigHolder {

  val consul = com.orbitz.consul.Consul.builder().withUrl(s"http://${config.getString("consul.host")}:8500").build()

  def getServiceAddresses(implicit actorSystem: ActorSystem): List[Address] = {
    val serviceName = config.getString("service.name")

    val queryOpts = ImmutableQueryOptions
      .builder()
      .consistencyMode(ConsistencyMode.CONSISTENT)
      .build()
    val serviceNodes = consul.healthClient().getHealthyServiceInstances(serviceName, queryOpts)

    serviceNodes.getResponse.toList map { node =>
      Address("akka.tcp", actorSystem.name, node.getService.getAddress, node.getService.getPort)
    }
  }
}
