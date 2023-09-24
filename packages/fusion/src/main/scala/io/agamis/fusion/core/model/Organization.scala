package io.agamis.fusion.core.model

import java.util.UUID
import io.agamis.fusion.core.model.common.TimeTracked
import java.sql.Timestamp

final case class Organization(
    id: UUID,
    label: String,
    queryable: Boolean,
    createdAt: Timestamp,
    updatedAt: Timestamp
) extends TimeTracked
