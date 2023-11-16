package io.agamis.fusion.core.model

import io.agamis.fusion.core.model.common.TimeTracked

import java.util.UUID
import java.time.LocalDateTime

final case class Group(
    id: UUID,
    name: String,
    fk: _ => {
        def organizationId: UUID
    },
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime
) extends TimeTracked
