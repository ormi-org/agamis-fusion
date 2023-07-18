package io.agamis.fusion

import com.typesafe.config.ConfigFactory
import io.agamis.fusion.Core

object Main {
  
  private val appConfig = ConfigFactory.load()

  def main(args: Array[String]): Unit = {
    val clusterName = appConfig.getString("clustering.cluster.name")
    Core.startNode(Core.Behavior.Root(), clusterName, appConfig)
  }
}
