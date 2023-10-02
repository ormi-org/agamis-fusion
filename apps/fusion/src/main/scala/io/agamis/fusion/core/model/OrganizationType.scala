package io.agamis.fusion.core.model

import io.agamis.fusion.core.model.common.TimeTracked

import java.time.LocalDateTime
import java.util.UUID

final case class OrganizationType(
    id: UUID,
    labelTextId: UUID,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime
) extends TimeTracked
