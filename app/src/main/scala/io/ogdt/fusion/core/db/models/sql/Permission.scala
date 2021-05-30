package io.ogdt.fusion.core.db.models.sql

import org.apache.ignite.cache.query.annotations.QuerySqlField
import io.ogdt.fusion.core.db.datastores.sql.PermissionStore
import io.ogdt.fusion.core.db.models.sql.typed.Model
import scala.concurrent.{ExecutionContext, Future}

class Permission(implicit @transient protected val store: PermissionStore) extends Model {

    @QuerySqlField(name = "editable", notNull = false)
    private var _editable: Boolean = false
    def editable: Boolean = _editable
    def setEditable: Permission = {
        _editable = true
        this
    }
    def setReadonly: Permission = {
        _editable = false
        this
    }

    /** @inheritdoc */
    def persist(implicit ec: ExecutionContext): Future[Unit] = {
        Future.unit
    }

    /** @inheritdoc */
    def remove(implicit ec: ExecutionContext): Future[Unit] = {
        Future.unit
    }
}