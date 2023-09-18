package io.agamis.fusion.api.rest.routes


import scala.concurrent.duration._


import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes

import akka.util.Timeout

import akka.actor.typed.ActorSystem

import io.agamis.fusion.api.rest.model.dto.organizationtype.{OrganizationTypeDto, OrganizationTypeJsonSupport}

/** Class Organization Routes
  *
  * @param system
  */
class OrganizationTypeRoutes(implicit system: ActorSystem[_]) extends OrganizationTypeJsonSupport {


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
            entity(as[OrganizationTypeDto]) { _ =>
              complete(StatusCodes.NotImplemented)
            }
          }
        )
      ),
      pathPrefix("organization-type")(
        concat(
          pathPrefix(Segment) { _: String =>
            concat(
              //get by id
              get {
                path(Segment) { _: String =>
                  complete(StatusCodes.NotImplemented)
                }
              },
              // update organizationType
              put {
                path(Segment) { _: String =>
                  entity(as[OrganizationTypeDto]) { _ =>
                    complete(StatusCodes.NotImplemented)
                  }
                }
              },
              // delete organizationType
              delete {
                path(Segment) { _: String =>
                  complete(StatusCodes.NotImplemented)
                }
              }
            )
          }
        )
      )
    )
}
