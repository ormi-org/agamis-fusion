package io.ogdt.fusion.core.db.models.sql

import io.ogdt.fusion.core.db.datastores.sql.UserStore
import io.ogdt.fusion.core.db.models.sql.typed.Model

import org.apache.ignite.cache.query.annotations.QuerySqlField

import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import java.sql.Timestamp

class User(implicit protected val store: UserStore) extends Model {

    @QuerySqlField(index = true, name = "id", notNull = true)
    protected var _id: UUID = null
    def id: UUID = _id
    // Used to set UUID (mainly for setting uuid of existing user when fetching)
    def setId(id: String): User = {
        _id = UUID.fromString(id)
        this
    }

    @QuerySqlField(name = "username", notNull = true)
    private var _username: String = null
    def username: String = _username
    def setUsername(username: String): User = {
        _username = username
        this
    }

    @QuerySqlField(name = "password", notNull = true)
    private var _password: String = null
    def password: String = _password
    def setPassword(password: String): User = {
        _password = password
        this
    }

    private var _relatedProfiles: List[Profile] = List()
    def relatedProfiles: List[Profile] = _relatedProfiles
    def addRelatedProfile(profile: Profile): User = {
        _relatedProfiles ::= profile
        profile.setRelatedUser(this)
        this
    }
    def deleteRelatedProfile(profile: Profile): User = {
        _relatedProfiles = _relatedProfiles.filter(p => p.id != profile.id)
        this
    }

    def persist(implicit ec: ExecutionContext): Future[Unit] = {
        store.persistUser(this)
    }

    def remove(implicit ec: ExecutionContext): Future[Unit] = {
        store.deleteUser(this)
    }
}