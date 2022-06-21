package io.agamis.fusion.external.api.rest

import akka.actor.typed.PostStop
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.Http

import scala.util.{Success, Failure}
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

import scala.concurrent.Future

import io.agamis.fusion.external.api.rest.routes.{
  FileRoutes,
  UserRoutes,
  GroupRoutes,
  ProfileRoutes,
  FileSystemRoutes,
  PermissionRoutes,
  OrganizationRoutes,
  AuthenticationRoutes
}

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import java.util.UUID
import akka.http.scaladsl.model.StatusCodes
import io.agamis.fusion.external.api.rest.routes.OrganizationTypeRoutes
import akka.actor.typed.ActorSystem

object Server {

  object V1 {
    def apply(host: String, port: Int, parentSystem: ActorSystem[_]): Future[ServerBinding] = {
      implicit val system = parentSystem

      val topLevel: Route =
        concat(
          pathPrefix("api")(
            concat(
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
