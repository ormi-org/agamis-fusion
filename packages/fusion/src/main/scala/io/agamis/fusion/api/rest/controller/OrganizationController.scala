package io.agamis.fusion.api.rest.controller

import akka.actor.typed.ActorSystem
import io.agamis.fusion.api.rest.model.dto.organization.OrganizationApiJsonSupport
import io.agamis.fusion.api.rest.model.dto.organization.OrganizationApiResponse
import io.agamis.fusion.api.rest.model.dto.organization.OrganizationDto
import io.agamis.fusion.core.actor.entity.Organization

class OrganizationController()(implicit system: ActorSystem[_])
    extends BehaviorBoundController[
      Organization.Command,
      OrganizationDto
    ]
    with OrganizationApiJsonSupport {

    override protected def mapToApiResponse(
        c: Organization.Command
    ): OrganizationApiResponse = ???

    override protected def excludeFields(
        user: OrganizationDto
    ): OrganizationDto = ???
}
