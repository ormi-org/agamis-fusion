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

import io.agamis.fusion.external.api.rest.dto.user.{UserDto, UserJsonSupport}
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.ResponseEntity
import akka.http.scaladsl.model.StatusCode

/** Class User Routes
  *
  * @param userRepository
  * @param system
  */
class UserRoutes(implicit system: ActorSystem[_]) extends UserJsonSupport {

  import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
  import akka.actor.typed.scaladsl.AskPattern.Askable

  // asking someone requires a timeout and a scheduler, if the timeout hits without response
  // the ask is failed with a TimeoutException
  implicit val timeout = Timeout(3.seconds)

  lazy val routes: Route =
    concat(
      pathPrefix("users")(
        concat(
          // get all users
          get {
            complete(StatusCodes.NotImplemented)
          },
          // create user
          post {
            entity(as[UserDto]) { user =>
              complete(StatusCodes.NotImplemented)
            }
          },
        )
      ),
      pathPrefix("user")(
        concat(
          //get by id
          get {
            path(Segment) { id: String =>
              complete(StatusCodes.NotImplemented)
            }
          },
          // update user
          put {
            path(Segment) { id: String =>
              entity(as[UserDto]) { user =>
                complete(StatusCodes.NotImplemented)
              }
            }
          },
          // delete user
          delete {
            path(Segment) { id: String =>
              complete(StatusCodes.NotImplemented)
            }
          }
        )
      )
    )
}
