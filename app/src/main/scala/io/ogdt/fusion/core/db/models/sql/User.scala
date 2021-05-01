package io.ogdt.fusion.core.db.models.sql

import io.ogdt.fusion.core.db.datastores.sql.UserStore
import io.ogdt.fusion.core.db.models.sql.typed.Model

import org.apache.ignite.cache.query.annotations.QuerySqlField

import java.util.UUID
import scala.concurrent.Future
import io.ogdt.fusion.core.db.models.sql.typed.annotations.{PrePersist, PostRemove}
import scala.concurrent.ExecutionContext

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

    private var _relatedProfile: Profile = null
    def relatedProfile: Profile = _relatedProfile
    def setRelatedProfile(profile: Profile): User = {
        _relatedProfile = profile.setRelatedUser(this)
        this
    }

    private def persist(implicit ec: ExecutionContext): Future[Boolean] = {
        super.persist(() => store.persistUser(this))
    }

    private def remove(implicit ec: ExecutionContext): Future[Boolean] = {
        super.persist(() => store.removeUser(this))
    }

    @PrePersist
    private def persistRelatedProfileModifications(implicit ec: ExecutionContext): Future[Boolean] = {
        _relatedProfile.persist
    }

    @PostRemove
    private def removeRelatedProfile(implicit ec: ExecutionContext): Future[Boolean] = {
        _relatedProfile.remove
    }
}