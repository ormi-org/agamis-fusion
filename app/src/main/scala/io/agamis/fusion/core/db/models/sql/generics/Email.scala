package io.agamis.fusion.core.db.models.sql.generics

import org.apache.ignite.cache.query.annotations.QuerySqlField

import java.util.UUID

/** A class that reflects the state of the profile entity in the database */
class Email {

    @QuerySqlField(name = "id", notNull = true, index = true)
    protected var _id: UUID = UUID.randomUUID()
    def id: UUID = _id
    // Used to set UUID (mainly for setting uuid of existing email when fetching)
    def setId(id: String): Email = {
        _id = UUID.fromString(id)
        this
    }

    @QuerySqlField(name = "address", notNull = true, index = true)
    private var _address: String = _
    def address: String = _address
    def setAddress(address: String): Email = {
        _address = address
        this
    }
}

object Email {
    def apply: Email = {
        new Email()
    }
}
