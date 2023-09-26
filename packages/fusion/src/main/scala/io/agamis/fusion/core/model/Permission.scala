package io.agamis.fusion.core.model

import io.agamis.fusion.core.model.common.TimeTracked
import java.util.UUID
import java.time.LocalDateTime

final case class Permission(
    id: UUID,
    label: String,
    queryable: Boolean,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime
) extends TimeTracked
