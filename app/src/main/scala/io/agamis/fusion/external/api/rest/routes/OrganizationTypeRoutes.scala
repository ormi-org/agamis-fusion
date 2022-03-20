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

import io.agamis.fusion.external.api.rest.dto.organizationtype.{
  OrganizationTypeDto,
  OrganizationTypeJsonSupport
}

/** Class Organization Routes
  *
  * @param system
  */
class OrganizationTypeRoutes(implicit system: ActorSystem[_]) extends OrganizationTypeJsonSupport {

  import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
  import akka.actor.typed.scaladsl.AskPattern.Askable

  // asking someone requires a timeout and a scheduler, if the timeout hits without response
  // the ask is failed with a TimeoutException
  implicit val timeout = Timeout(3.seconds)

  lazy val routes: Route =
    concat(
      pathPrefix("organization-types")(
        concat(
          // get all organizations
          get {
            complete(StatusCodes.NotImplemented)
          },
          // create organization
          post {
            entity(as[OrganizationTypeDto]) { organizationType =>
              complete(StatusCodes.NotImplemented)
            }
          }
        )
      ),
      pathPrefix("organization-type")(
        concat(
          pathPrefix(Segment) { organizationUuid: String =>
            concat(
              //get by id
              get {
                path(Segment) { id: String =>
                  complete(StatusCodes.NotImplemented)
                }
              },
              // update organizationType
              put {
                path(Segment) { id: String =>
                  entity(as[OrganizationTypeDto]) { organizationType =>
                    complete(StatusCodes.NotImplemented)
                  }
                }
              },
              // delete organizationType
              delete {
                path(Segment) { id: String =>
                  complete(StatusCodes.NotImplemented)
                }
              }
            )
          }
        )
      )
    )
}
