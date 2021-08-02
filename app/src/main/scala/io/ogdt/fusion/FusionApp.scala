package io.ogdt.fusion

import com.typesafe.config.ConfigException

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import io.ogdt.fusion.external.http.actors.Server
import io.ogdt.fusion.core.fs.actors.FusionFS

object FusionApp {
    
    def main(args: Array[String]): Unit = {
        
        //implicit val system = ActorSystem(Server("localhost", 8080), "FusionSystem")
        val system = ActorSystem[FusionFS.Command](FusionFS(), "fusion-system")

        val fusionfs: ActorRef[FusionFS.Command] = system

        if (args.contains("--init")) {
            // init db
            fusionfs ! FusionFS.InitDb
        }

        implicit val executionContext = system.executionContext
    }
}