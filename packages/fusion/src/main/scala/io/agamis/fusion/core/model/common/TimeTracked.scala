package io.agamis.fusion.core.model.common

import java.sql.Timestamp

trait TimeTracked {
    def createdAt: Timestamp
    def updatedAt: Timestamp
}
