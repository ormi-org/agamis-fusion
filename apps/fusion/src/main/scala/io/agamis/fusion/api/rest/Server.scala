package io.agamis.fusion.api.rest

import io.agamis.fusion.api.rest.controller.OrganizationController
import io.agamis.fusion.api.rest.routes.OrganizationRoutes
import io.agamis.fusion.api.rest.routes.apps.AppRoutes
import io.agamis.fusion.core.shard.OrganizationShard
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.cluster.sharding.typed.scaladsl.ClusterSharding
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.Http.ServerBinding
import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.Route
import spray.json.DefaultJsonProtocol

import scala.concurrent.Future

object Server {

    object TopLevelApiMessage
        extends SprayJsonSupport
        with DefaultJsonProtocol {

        case class Message(
            val apiVersions: List[String] = List("v1")
        )

        implicit val MessageFormat = jsonFormat1(Message)

        def apply(): String = {
            Message().toJson.prettyPrint
        }
    }

    object V1 {
        def apply(
            host: String,
            port: Int,
            parentSystem: ActorSystem[Nothing]
        ): Future[ServerBinding] = {
            implicit val system   = parentSystem
            implicit val sharding = ClusterSharding(system)

            implicit val ec = system.executionContext

            val topLevel: Route =
                concat(
                  pathPrefix("app")(
                    new AppRoutes().routes
                  ),
                  path("api")(
                    concat(
                      get {
                          complete(TopLevelApiMessage())
                      }
                    )
                  ),
                  pathPrefix("api")(
                    concat(
                      new OrganizationRoutes(
                        new OrganizationController(OrganizationShard)
                      ).routes
                    )
                  )
                )
            Http().newServerAt(host, port).bind(topLevel)
        }
    }
}
