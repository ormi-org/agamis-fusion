package io.agamis.fusion.core.model

import io.agamis.fusion.core.model.common.TimeTracked

import java.time.LocalDateTime
import java.util.UUID

final case class Organization(
    id: UUID,
    label: String,
    queryable: Boolean,
    fk: OrganizationFK,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime
) extends TimeTracked

final case class OrganizationFK(
    organizationTypeId: UUID
)
