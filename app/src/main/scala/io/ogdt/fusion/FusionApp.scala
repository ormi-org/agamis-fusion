package io.ogdt.fusion

import com.typesafe.config.ConfigException

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import io.ogdt.fusion.external.http.actors.Server

object FusionApp {
    
    def main(args: Array[String]): Unit = {
        implicit val system = ActorSystem(Server("localhost", 8080), "FusionSystem")

        implicit val executionContext = system.executionContext
    }
}