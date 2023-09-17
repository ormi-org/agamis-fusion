package io.agamis.fusion.api.rest.routes

import scala.util.Success
import scala.util.Failure

import scala.concurrent.duration._
import scala.concurrent.Future

import java.util.UUID

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpEntity, ContentTypes, StatusCodes}

import akka.util.Timeout

import akka.actor.typed.{ActorSystem, ActorRef}

import io.agamis.fusion.api.rest.model.dto.group.{GroupDto, GroupJsonSupport}

/** Class Group Routes
  *
  * @param buildGroupRepository
  * @param system
  */
class GroupRoutes(implicit system: ActorSystem[_]) extends GroupJsonSupport {

  import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
  import akka.actor.typed.scaladsl.AskPattern.Askable

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
            entity(as[GroupDto]) { group =>
              complete(StatusCodes.NotImplemented)
            }
          },
        )
      ),
      pathPrefix("group")(
        concat(
          //get by id
          get {
            path(Segment) { id: String =>
              complete(StatusCodes.NotImplemented)
            }
          },
          // update group
          put {
            path(Segment) { id: String =>
              entity(as[GroupDto]) { group =>
                complete(StatusCodes.NotImplemented)
              }
            }
          },
          // delete group
          delete {
            path(Segment) { id: String =>
              complete(StatusCodes.NotImplemented)
            }
          }
        )
      )
    )
}
