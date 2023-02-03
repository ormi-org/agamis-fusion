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

import io.agamis.fusion.external.api.rest.dto.organization.{
  OrganizationDto,
  OrganizationJsonSupport
}

/** Class Organization Routes
  *
  * @param buildOrganizationRepository
  * @param system
  */
class OrganizationRoutes(implicit system: ActorSystem[_])
    extends OrganizationJsonSupport {

  import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
  import akka.actor.typed.scaladsl.AskPattern.Askable

  // asking someone requires a timeout and a scheduler, if the timeout hits without response
  // the ask is failed with a TimeoutException
  implicit val timeout = Timeout(3.seconds)

  lazy val routes: Route =
    concat(
      pathPrefix("organizations")(
        concat(
          // get all organizations
          get {
            complete(StatusCodes.NotImplemented)
          },
          // create organization
          post {
            entity(as[OrganizationDto]) { organization =>
              complete(StatusCodes.NotImplemented)
            }
          }
        )
      ),
      pathPrefix("organization")(
        concat(
          // get by id
          get {
            path(Segment) { id: String =>
              complete(StatusCodes.NotImplemented)
            }
          },
          // update organization
          put {
            path(Segment) { id: String =>
              entity(as[OrganizationDto]) { organization =>
                complete(StatusCodes.NotImplemented)
              }
            }
          },
          // delete organization
          delete {
            path(Segment) { id: String =>
              complete(StatusCodes.NotImplemented)
            }
          }
        )
      )
    )
}
