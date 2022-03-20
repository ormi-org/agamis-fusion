package io.agamis.fusion

import akka.actor.typed.{ActorRef, ActorSystem}
import io.agamis.fusion.Core

import scala.concurrent.ExecutionContextExecutor

object Main {

  def main(args: Array[String]): Unit = {

    val system = ActorSystem[Core.Command](Core(), "fusion-system")

    val core: ActorRef[Core.Command] = system

    if (args.contains("--init-db")) {
      // init db
      core ! Core.InitDb
    }

    implicit val executionContext: ExecutionContextExecutor = system.executionContext
  }
}
