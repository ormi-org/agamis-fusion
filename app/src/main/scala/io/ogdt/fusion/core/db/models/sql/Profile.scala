package io.ogdt.fusion.core.db.models.sql

import io.ogdt.fusion.core.db.datastores.sql.ProfileStore

import io.ogdt.fusion.core.db.models.sql.typed.Model
import org.apache.ignite.cache.query.annotations.QuerySqlField
import java.util.UUID
import java.time.Instant
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class Profile(implicit protected val store: ProfileStore) extends Model {

    @QuerySqlField(index = true, name = "id", notNull = true)
    protected var _id: UUID = null
    def id: UUID = _id
    def setId(id: String): Profile = {
        _id = UUID.fromString(id)
        this
    }

    @QuerySqlField(name = "lastname", notNull = true)
    private var _lastname: String = null
    def lastname: String = _lastname
    def setLastname(lastname: String): Profile = {
        _lastname = lastname
        this
    }

    @QuerySqlField(name = "firstname", notNull = true)
    private var _firstname: String = null
    def firstname: String = _firstname
    def setFirstname(firstname: String): Profile = {
        _firstname = firstname
        this
    }

    @QuerySqlField(name = "last_login", notNull = true)
    private var _lastLogin: Instant = null
    def lastLogin: Instant = _lastLogin
    def setLastLogin(lastLogin: Instant): Profile = {
        _lastLogin = lastLogin
        this
    }

    @QuerySqlField(name = "user_id", notNull = true)
    private var _userId: UUID = null

    private var _relatedUser: User = null
    def relatedUser: User = _relatedUser
    def setRelatedUser(user: User): Profile = {
        _relatedUser = user
        _userId = user.id
        this
    }

    def persist(implicit ec: ExecutionContext): Future[Boolean] = {
        super.persist(() => store.persistProfile(this))
    }

    def remove(implicit ec: ExecutionContext): Future[Boolean] = {
        super.persist(() => store.removeProfile(this))
    }
}
