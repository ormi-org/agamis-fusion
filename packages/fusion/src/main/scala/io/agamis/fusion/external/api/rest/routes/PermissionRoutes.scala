package io.agamis.fusion.external.api.rest.routes

import akka.actor.typed.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import io.agamis.fusion.external.api.rest.dto.permission.PermissionDto
import io.agamis.fusion.external.api.rest.dto.permission.PermissionJsonSupport

import scala.concurrent.duration._

/** Class Permissions Routes
  *
  * @param system
  */
class PermissionRoutes(implicit system: ActorRef[_]) extends PermissionJsonSupport {


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
