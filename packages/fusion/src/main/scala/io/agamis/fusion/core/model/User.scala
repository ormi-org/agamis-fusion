package io.agamis.fusion.core.model

import io.agamis.fusion.core.model.common.TimeTracked

import java.util.UUID
import java.sql.Timestamp

final case class User(
    id: UUID,
    username: String,
    password: String,
    createdAt: Timestamp,
    updatedAt: Timestamp
) extends TimeTracked
