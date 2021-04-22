package io.ogdt.fusion

import com.typesafe.config.ConfigException

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{HttpEntity, ContentTypes, StatusCodes}
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.Http

import io.ogdt.fusion.external.routes.FileRouter

object FusionApp {
    
    def main(args: Array[String]): Unit = {
        implicit val system = ActorSystem(Behaviors.empty, "FusionSystem")

        implicit val executionContext = system.executionContext

        val topLevel: Route = 
            concat(
                path("api")(
                    concat(
                        path("v1")(
                            concat(
                                path("fs")(
                                    concat(
                                        pathPrefix("file")(FileRouter.routes)
                                    )
                                )
                            )
                        )
                    ) 
                )
            )

        val bindingFuture = Http().newServerAt("localhost", 8080).bind(topLevel)

        println("Connected to http://localhost:8080")
    }
}