package io.agamis.fusion.external.api.rest.routes

import scala.util.Success
import scala.util.Failure

import scala.concurrent.duration._

import java.util.UUID

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{HttpEntity, ContentTypes, StatusCodes}

import akka.util.Timeout

import akka.actor.typed.{ActorSystem, ActorRef}
import scala.concurrent.Future
import io.agamis.fusion.external.api.rest.dto.permission.{
  PermissionDto,
  PermissionJsonSupport
}
import akka.cluster.sharding.typed.ShardingEnvelope
import io.agamis.fusion.core.actors.data.DataActor

/** Class Permissions Routes
  *
  * @param system
  */
class PermissionRoutes(implicit system: ActorRef[_]) extends PermissionJsonSupport {

  import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
  import akka.actor.typed.scaladsl.AskPattern.Askable

  implicit val timeout = Timeout(3.seconds)

  lazy val routes: Route =
    concat(
      pathPrefix("permissions")(
        concat(
          // get all permissions
          get {
            complete(StatusCodes.NotImplemented)
          },
          // create permission
          post {
            entity(as[PermissionDto]) { permission =>
              complete(StatusCodes.NotImplemented)
            }
          }
        )
      ),
      pathPrefix("permission")(
        concat(
          // get by id
          get {
            path(Segment) { id: String =>
              complete(StatusCodes.NotImplemented)
            }
          },
          // update permission
          put {
            path(Segment) { id: String =>
              entity(as[PermissionDto]) { permission =>
                complete(StatusCodes.NotImplemented)
              }
            }
          },
          // delete permission
          delete {
            path(Segment) { id: String =>
              complete(StatusCodes.NotImplemented)
            }
          }
        )
      )
    )
}
