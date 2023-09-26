package io.agamis.fusion.core.model

import io.agamis.fusion.core.model.common.TimeTracked

import java.time.LocalDateTime
import java.util.UUID

final case class Filesystem(
    id: UUID,
    rootdirId: String,
    label: String,
    shared: Boolean,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime
) extends TimeTracked
