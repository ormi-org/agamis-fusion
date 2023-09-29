package io.agamis.fusion.api.rest.controller

import akka.actor.typed.ActorSystem
import io.agamis.fusion.api.rest.model.dto.organization.OrganizationApiJsonSupport
import io.agamis.fusion.core.actor.entity.Organization
import scala.util.Try
import java.util.UUID

class OrganizationController()(implicit system: ActorSystem[_])
    extends ActorSystemController
    with OrganizationApiJsonSupport {
    import io.agamis.fusion.core.actor.entity.Organization._

    // def getSingleOrganization(id: String): Directive[State] = {
    //     Try {
    //         UUID.fromString(id)
    //     }
    // }
}
