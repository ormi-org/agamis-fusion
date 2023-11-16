package io.agamis.fusion.api.rest.routes

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import io.agamis.fusion.api.rest.controller.OrganizationController
import io.agamis.fusion.api.rest.model.dto.organization.OrganizationJsonSupport
import io.agamis.fusion.api.rest.model.dto.organization.OrganizationMutation
import io.agamis.fusion.api.rest.model.dto.organization.OrganizationMutationJsonSupport

import scala.concurrent.duration._

/** Class Organization Routes
  *
  * @param buildOrganizationRepository
  * @param system
  */
class OrganizationRoutes(organizationController: OrganizationController)(
    implicit system: ActorSystem[_]
) extends OrganizationJsonSupport
    with OrganizationMutationJsonSupport {

    implicit val DEFAULT_TIMEOUT = Timeout(2.seconds)

    lazy val routes: Route =
        concat(
          pathPrefix("organizations")(
            concat(
              // create organization
              post {
                  entity(as[OrganizationMutation]) { mut =>
                      organizationController.createOrganization(mut)
                  }
              },
              // get by id
              get {
                  path(Segment) { id: String =>
                      organizationController.getSingleOrganization(id)
                  }
              },
              // update organization
              put {
                  path(Segment) { id: String =>
                      entity(as[OrganizationMutation]) { mut =>
                          organizationController.updateOrganization(id, mut)
                      }
                  }
              },
              // delete organization
              delete {
                  path(Segment) { id: String =>
                      organizationController.deleteOrganization(id)
                  }
              }
            )
          )
        )
}
