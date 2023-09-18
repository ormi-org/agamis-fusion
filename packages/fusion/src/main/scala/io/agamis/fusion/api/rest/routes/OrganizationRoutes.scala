package io.agamis.fusion.api.rest.routes


import scala.concurrent.duration._


import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes

import akka.util.Timeout

import akka.actor.typed.ActorSystem

import io.agamis.fusion.api.rest.model.dto.organization.{OrganizationDto, OrganizationJsonSupport}

/** Class Organization Routes
  *
  * @param buildOrganizationRepository
  * @param system
  */
class OrganizationRoutes(implicit system: ActorSystem[_])
    extends OrganizationJsonSupport {


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
            entity(as[OrganizationDto]) { _ =>
              complete(StatusCodes.NotImplemented)
            }
          }
        )
      ),
      pathPrefix("organization")(
        concat(
          // get by id
          get {
            path(Segment) { _: String =>
              complete(StatusCodes.NotImplemented)
            }
          },
          // update organization
          put {
            path(Segment) { _: String =>
              entity(as[OrganizationDto]) { _ =>
                complete(StatusCodes.NotImplemented)
              }
            }
          },
          // delete organization
          delete {
            path(Segment) { _: String =>
              complete(StatusCodes.NotImplemented)
            }
          }
        )
      )
    )
}
