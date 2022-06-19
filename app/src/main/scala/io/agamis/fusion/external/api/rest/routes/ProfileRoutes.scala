package io.agamis.fusion.external.api.rest.routes

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import io.agamis.fusion.external.api.rest.dto.profile.ProfileDto
import io.agamis.fusion.external.api.rest.dto.profile.ProfileJsonSupport

import scala.concurrent.duration._

/** Class Profile Routes
  *
  * @param system
  */
class ProfileRoutes(implicit system: ActorSystem[_]) extends ProfileJsonSupport {


  // asking someone requires a timeout and a scheduler, if the timeout hits without response
  // the ask is failed with a TimeoutException
  implicit val timeout = Timeout(3.seconds)

  lazy val routes: Route =
    concat(
      pathPrefix("profiles")(
        concat(
          // get all profiles
          get {
            complete(StatusCodes.NotImplemented)
          },
          // create profile
          post {
            entity(as[ProfileDto]) { profile =>
              complete(StatusCodes.NotImplemented)
            }
          },
        )
      ),
      pathPrefix("profile")(
        concat(
          //get by id
          get {
            path(Segment) { id: String =>
              complete(StatusCodes.NotImplemented)
            }
          },
          // update profile
          put {
            path(Segment) { id: String =>
              entity(as[ProfileDto]) { profile =>
                complete(StatusCodes.NotImplemented)
              }
            }
          },
          // delete profile
          delete {
            path(Segment) { id: String =>
              complete(StatusCodes.NotImplemented)
            }
          }
        )
      )
    )
}
