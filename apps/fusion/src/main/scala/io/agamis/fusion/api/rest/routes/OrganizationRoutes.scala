package io.agamis.fusion.api.rest.routes

import io.agamis.fusion.api.rest.controller.OrganizationController
import io.agamis.fusion.api.rest.model.dto.organization.OrganizationJsonSupport
import io.agamis.fusion.api.rest.model.dto.organization.OrganizationMutation
import io.agamis.fusion.api.rest.model.dto.organization.OrganizationMutationJsonSupport
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.Route
import org.apache.pekko.util.Timeout

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
