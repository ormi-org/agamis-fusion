package io.agamis.fusion.core.model.common

import java.time.LocalDateTime

trait TimeTracked {
    def createdAt: LocalDateTime
    def updatedAt: LocalDateTime
}
