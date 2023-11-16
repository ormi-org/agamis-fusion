package io.agamis.fusion.core.model

import io.agamis.fusion.core.model.common.TimeTracked

import java.time.LocalDateTime
import java.util.UUID

final case class User(
    id: UUID,
    username: String,
    password: String,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime
) extends TimeTracked
