package io.agamis.fusion.external.api.rest.routes

import scala.util.Success
import scala.util.Failure

import scala.concurrent.Future
import scala.concurrent.duration._

import java.util.UUID

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{HttpEntity, ContentTypes, StatusCodes}

import akka.util.Timeout

import akka.actor.typed.{ActorSystem, ActorRef}

import io.agamis.fusion.external.api.rest.dto.profile.{
  ProfileDto,
  ProfileJsonSupport
}

/** Class Profile Routes
  *
  * @param system
  */
class ProfileRoutes(implicit system: ActorSystem[_]) extends ProfileJsonSupport {

  import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
  import akka.actor.typed.scaladsl.AskPattern.Askable

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
