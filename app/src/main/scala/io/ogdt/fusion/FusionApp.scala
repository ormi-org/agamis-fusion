package io.ogdt.fusion

// import db.DbHandler
import env.EnvContainer
import com.typesafe.config.ConfigException
import io.ogdt.fusion.core.db.ignite.IgniteClientNodeWrapper
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import io.ogdt.fusion.core.fs.actors.FusionFS
import akka.actor.typed.ActorRef

object FusionApp {
    
    def main(args: Array[String]): Unit = {
        val system = ActorSystem[FusionFS.Command](FusionFS(), "fusion-system")
        // val fusionfs: ActorRef[FusionFS.Command] = system

        // fusionfs ! FusionFS.GracefulShutdown
    }
}