package io.agamis.fusion

import akka.actor.typed.{ActorRef, ActorSystem}
import io.agamis.fusion.core.fs.FileSystem

import scala.concurrent.ExecutionContextExecutor

object Main {

  def main(args: Array[String]): Unit = {

    //implicit val system = ActorSystem(Server("localhost", 8080), "FusionSystem")
    val system = ActorSystem[FileSystem.Command](FileSystem(), "fusion-system")

    val fusionFs: ActorRef[FileSystem.Command] = system

    if (args.contains("--init")) {
      // init db
      fusionFs ! FileSystem.InitDb
    }

    implicit val executionContext: ExecutionContextExecutor = system.executionContext
  }
}
