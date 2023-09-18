package io.agamis.fusion.api.rest.routes


import scala.concurrent.duration._


import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes

import akka.util.Timeout

import akka.actor.typed.ActorSystem

import io.agamis.fusion.api.rest.model.dto.group.{GroupDto, GroupJsonSupport}

/** Class Group Routes
  *
  * @param buildGroupRepository
  * @param system
  */
class GroupRoutes(implicit system: ActorSystem[_]) extends GroupJsonSupport {


  // asking someone requires a timeout and a scheduler, if the timeout hits without response
  // the ask is failed with a TimeoutException
  implicit val timeout = Timeout(3.seconds)

  lazy val routes: Route =
    concat(
      pathPrefix("groups")(
        concat(
          // get all groups
          get {
            complete(StatusCodes.NotImplemented)
          },
          // create group
          post {
            entity(as[GroupDto]) { _ =>
              complete(StatusCodes.NotImplemented)
            }
          },
        )
      ),
      pathPrefix("group")(
        concat(
          //get by id
          get {
            path(Segment) { _: String =>
              complete(StatusCodes.NotImplemented)
            }
          },
          // update group
          put {
            path(Segment) { _: String =>
              entity(as[GroupDto]) { _ =>
                complete(StatusCodes.NotImplemented)
              }
            }
          },
          // delete group
          delete {
            path(Segment) { _: String =>
              complete(StatusCodes.NotImplemented)
            }
          }
        )
      )
    )
}
