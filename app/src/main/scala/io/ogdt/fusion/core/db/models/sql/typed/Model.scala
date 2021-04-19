package io.ogdt.fusion.core.db.models.sql.typed

import java.util.UUID

trait Model {
    // TODO : trouver un moyen de valider la covariance
    // protected val store: SqlStore[UUID, Model]

    genId()

    protected var _id: UUID
    private def genId() = {
        _id = UUID.randomUUID()
    }
}
