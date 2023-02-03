package io.agamis.fusion

import akka.actor.typed.{ActorRef, ActorSystem}
import io.agamis.fusion.Core

import scala.concurrent.ExecutionContextExecutor
import com.typesafe.config.ConfigFactory

object Main {
  
  private val appConfig = ConfigFactory.load()

  def main(args: Array[String]): Unit = {
    val clusterName = appConfig.getString("clustering.cluster.name")
    val clusterPort = appConfig.getInt("clustering.port")
    val defaultPort = appConfig.getInt("clustering.defaultPort")
    Core.startNode(Core.Behavior.Root(clusterPort, defaultPort), clusterName, appConfig)
  }
}
