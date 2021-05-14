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

    @QuerySqlField(index = true, name = "id", notNull = true)
    protected var _id: UUID = UUID.randomUUID()

    /** Entity Universal Unique IDentifier property
      *
      * @return [[java.util.UUID UUID]] of this entity
      */
    def id: UUID = _id

    /** A method for setting Universal Unique IDentifier property
      *
      * Used to set UUID (mainly for setting uuid of existing user when fetching)
      * 
      * @param id a [[java.util.UUID UUID]] to assign to language unique id property
      * @return this object
      */
    def setId(id: String): this.type = {
        _id = UUID.fromString(id)
        this
    }

    @QuerySqlField(name = "created_at", notNull = true)
    protected var _createdAt: Timestamp = null

    /** Entity property that reflects creation datetime of this entity
      *
      * @return creation date [[java.sql.Timestamp Timestamp]]
      */
    def createdAt: Timestamp = _createdAt

    /** A method for setting createdAt property
      * 
      * @param createdAt a [[java.sql.Timestamp Timestamp]] to assign to entity createdAt property
      * @return
      */
    def setCreatedAt(createdAt: Timestamp): this.type = {
        _createdAt = createdAt
        this
    }

    @QuerySqlField(name = "updated_at", notNull = true)
    protected var _updatedAt: Timestamp = null

    /** Entity property that reflects last update datetime of this entity
      *
      * @return creation date [[java.sql.Timestamp Timestamp]]
      */
    def updatedAt: Timestamp = _updatedAt

    /** A method for setting updatedAt property
      *
      * @param updatedAt a [[java.sql.Timestamp]] to assign to entity updatedAt property
      * @return this object
      */
    def setUpdatedAt(updatedAt: Timestamp): this.type = {
        _updatedAt = updatedAt
        this
    }

    /** A method for triggering a write operation of this entity to database
      *
      * @param ec implicit [[scala.concurrent.ExecutionContext ExecutionContext]]
      * @return a future success or failure
      */
    protected def persist(implicit ec: ExecutionContext): Future[Unit]

    /** A method for triggering a delete operation of this entity in database
      *
      * @param ec implicit [[scala.concurrent.ExecutionContext ExecutionContext]]
      * @return a future success or failure
      */
    protected def remove(implicit ec: ExecutionContext): Future[Unit]
}
