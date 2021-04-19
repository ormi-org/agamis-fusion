package io.ogdt.fusion.core.db.models.sql

import io.ogdt.fusion.core.db.datastores.sql.UserStore
import io.ogdt.fusion.core.db.models.sql.typed.Model

import org.apache.ignite.cache.query.annotations.QuerySqlField

import java.util.UUID

class User(protected val store: UserStore) extends Model {

    @QuerySqlField(index = true, name = "id")
    protected var _id: UUID = null
    def id: UUID = _id
    // Used to set UUID (mainly for setting uuid of existing user when fetching)
    def setId(id: String): User = {
        _id = UUID.fromString(id)
        this
    }

    @QuerySqlField(name = "username")
    private var _username: String = null
    def username: String = _username
    def setUsername(username: String): User = {
        _username = username
        this
    }

    @QuerySqlField(name = "password")
    private var _password: String = null
    def password: String = _password
    def setPassword(password: String): User = {
        _password = password
        this
    }

    def persist() = {
        store.persistUser(this)
    }
}