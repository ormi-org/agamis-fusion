package io.agamis.fusion

import com.typesafe.config.ConfigFactory
import io.agamis.fusion.Core

object Main {
  
  private val appConfig = ConfigFactory.load()

  def main(args: Array[String]): Unit = {
    val clusterName = appConfig.getString("clustering.cluster.name")
    val clusterPort = appConfig.getInt("clustering.port")
    val defaultPort = appConfig.getInt("clustering.defaultPort")
    Core.startNode(Core.Behavior.Root(clusterPort, defaultPort), clusterName, appConfig)
  }
}
