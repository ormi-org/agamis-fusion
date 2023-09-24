package io.agamis.fusion.core.model

import io.agamis.fusion.core.model.common.TimeTracked

import java.util.UUID
import java.sql.Timestamp

final case class Filesystem(
    id: UUID,
    rootdirId: String,
    label: String,
    shared: Boolean,
    createdAt: Timestamp,
    updatedAt: Timestamp
) extends TimeTracked
