package io.agamis.fusion.core.model

import io.agamis.fusion.core.model.common.TimeTracked

import java.time.LocalDateTime
import java.util.UUID

final case class Profile(
    id: UUID,
    firstname: String,
    lastname: String,
    lastLogin: LocalDateTime,
    isActive: Boolean,
    fk: _ => {
        def userId: UUID
        def organizationId: UUID
    },
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime
) extends TimeTracked
