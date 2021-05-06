package io.ogdt.fusion.core.db.models.sql.typed

import java.util.UUID
import scala.concurrent.Future
import scala.util.Success
import scala.concurrent.ExecutionContext
import scala.util.Failure
import io.ogdt.fusion.core.db.datastores.typed.SqlStore
import java.sql.Timestamp
import org.apache.ignite.cache.query.annotations.QuerySqlField

trait Model {

    genId()

    protected var _id: UUID
    private def genId() = {
        this._id = this._id match {
            case null => UUID.randomUUID()
            case x => x
        }
    }

    @QuerySqlField(name = "created_at", notNull = true)
    protected var _createdAt: Timestamp = null
    def createdAt: Timestamp = _createdAt
    def setCreatedAt(createdAt: Timestamp): this.type = {
        _createdAt = createdAt
        this
    }

    @QuerySqlField(name = "updated_at", notNull = true)
    protected var _updatedAt: Timestamp = null
    def updatedAt: Timestamp = _updatedAt
    def setUpdatedAt(updatedAt: Timestamp): this.type = {
        _updatedAt = updatedAt
        this
    }

    protected def persist(implicit ec: ExecutionContext): Future[Unit]

    protected def remove(implicit ec: ExecutionContext): Future[Unit]
}
