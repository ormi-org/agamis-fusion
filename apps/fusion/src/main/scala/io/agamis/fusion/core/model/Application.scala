package io.agamis.fusion.core.model

import io.agamis.fusion.core.model.common.TimeTracked
import io.agamis.fusion.core.model.common.ApplicationStatus
import java.util.UUID
import java.time.LocalDateTime

final case class Application(
    id: UUID,
    appUniversalId: String,
    version: String,
    status: ApplicationStatus,
    manifestUrl: String,
    storeUrl: String,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime
) extends TimeTracked
