package io.agamis.fusion.api.rest

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.agamis.fusion.core.services.UserService
import io.agamis.fusion.api.rest.routes.AuthenticationRoutes
import io.agamis.fusion.api.rest.routes.FileRoutes
import io.agamis.fusion.api.rest.routes.FileSystemRoutes
import io.agamis.fusion.api.rest.routes.GroupRoutes
import io.agamis.fusion.api.rest.routes.OrganizationRoutes
import io.agamis.fusion.api.rest.routes.OrganizationTypeRoutes
import io.agamis.fusion.api.rest.routes.PermissionRoutes
import io.agamis.fusion.api.rest.routes.ProfileRoutes
import io.agamis.fusion.api.rest.routes.UserRoutes
import io.agamis.fusion.api.rest.routes.apps.AppRoutes
import spray.json.DefaultJsonProtocol

import scala.concurrent.Future

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
              get {
                complete("Fusion API Route")
              },
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
      Http().newServerAt(host, port).bind(topLevel)
    }
  }
}
