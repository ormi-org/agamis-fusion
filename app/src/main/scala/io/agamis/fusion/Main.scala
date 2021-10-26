package io.agamis.fusion

import akka.actor.typed.{ActorRef, ActorSystem}
import io.agamis.fusion.core.fs.actors.FusionFileSystem

import scala.concurrent.ExecutionContextExecutor

object Main {

  def main(args: Array[String]): Unit = {

    //implicit val system = ActorSystem(Server("localhost", 8080), "FusionSystem")
    val system = ActorSystem[FusionFileSystem.Command](FusionFileSystem(), "fusion-system")

    val fusionFs: ActorRef[FusionFileSystem.Command] = system

    if (args.contains("--init")) {
      // init db
      fusionFs ! FusionFileSystem.InitDb
    }

    implicit val executionContext: ExecutionContextExecutor = system.executionContext
  }
}
