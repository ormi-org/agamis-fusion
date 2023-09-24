package io.agamis.fusion.core.model

import io.agamis.fusion.core.model.common.TimeTracked

import java.util.UUID
import java.sql.Timestamp

final case class OrganizationType(
    id: UUID,
    labelTextId: UUID,
    createdAt: Timestamp,
    updatedAt: Timestamp
) extends TimeTracked
