package io.agamis.fusion.external.api.rest

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.sharding.typed.scaladsl.Entity
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.agamis.fusion.core.actors.data.DataActor
import io.agamis.fusion.external.api.rest.routes.AuthenticationRoutes
import io.agamis.fusion.external.api.rest.routes.FileRoutes
import io.agamis.fusion.external.api.rest.routes.FileSystemRoutes
import io.agamis.fusion.external.api.rest.routes.GroupRoutes
import io.agamis.fusion.external.api.rest.routes.OrganizationRoutes
import io.agamis.fusion.external.api.rest.routes.OrganizationTypeRoutes
import io.agamis.fusion.external.api.rest.routes.PermissionRoutes
import io.agamis.fusion.external.api.rest.routes.ProfileRoutes
import io.agamis.fusion.external.api.rest.routes.UserRoutes

import scala.concurrent.Future
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import io.agamis.fusion.core.services.UserService

object Server {

  object TopLevelApiMessage extends SprayJsonSupport with DefaultJsonProtocol {

    case class Message(
      val apiVersions: List[String] = List("v1")
    )

    implicit val MessageFormat = jsonFormat1(Message)

    def apply(): String = {
      Message().toJson.prettyPrint
    }
  }

  object V1 {
    def apply(host: String, port: Int, parentSystem: ActorSystem[Nothing]): Future[ServerBinding] = {
      implicit val system = parentSystem
      implicit val sharding = ClusterSharding(system)
      implicit val userService: UserService = new UserService

      val topLevel: Route =
        concat(
          path("api")(
            concat(
              get {
                complete(TopLevelApiMessage())
              }
            )
          ),
          pathPrefix("api")(
            concat(
              path("v1")(
                concat(
                  get {
                    complete("Fusion API v1 Route")
                  }
                )
              ),
              pathPrefix("v1")(
                concat(
                  pathPrefix("auth")(
                    new AuthenticationRoutes().routes
                  ),
                  new FileSystemRoutes().routes,
                  new FileRoutes().routes,
                  new GroupRoutes().routes,
                  new OrganizationRoutes().routes,
                  new OrganizationTypeRoutes().routes,
                  new PermissionRoutes().routes,
                  new ProfileRoutes().routes,
                  new UserRoutes().routes
                )
              )
            )
          )
        )
      Http().newServerAt(host, port).bind(topLevel)
    }
  }
}
