package com.sap1ens

import akka.actor.Address
import com.orbitz.consul.option.{ConsistencyMode, ImmutableQueryOptions}
import com.sap1ens.utils.ConfigHolder

import scala.collection.JavaConversions._

object Consul extends ConfigHolder {

  val serviceName = config.getString("service.name")

  val consul = com.orbitz.consul.Consul.builder().withUrl(s"http://${config.getString("consul.host")}:8500").build()

  def getServiceAddresses: List[Address] = {

    val healthClient = consul.healthClient()
    val queryOpts = ImmutableQueryOptions.builder().consistencyMode(ConsistencyMode.CONSISTENT).build()
    val serviceNodes = healthClient.getHealthyServiceInstances(serviceName, queryOpts)

    serviceNodes.getResponse.toList map { node =>
      Address("akka.tcp", "microservice-system", node.getService.getAddress, node.getService.getPort)
    }
  }
}
