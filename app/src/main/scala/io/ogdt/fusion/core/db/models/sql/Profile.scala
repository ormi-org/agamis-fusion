package io.ogdt.fusion.core.db.models.sql

import io.ogdt.fusion.core.db.datastores.sql.ProfileStore

import io.ogdt.fusion.core.db.models.sql.typed.Model
import org.apache.ignite.cache.query.annotations.QuerySqlField
import java.util.UUID
import java.sql.Timestamp
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import java.time.Instant

class Profile(implicit protected val store: ProfileStore) extends Model {

    // setCreatedAt(Timestamp.from(Instant.now()))

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

    @QuerySqlField(name = "last_login", notNull = false)
    private var _lastLogin: Timestamp = null
    def lastLogin: Timestamp = _lastLogin
    def setLastLogin(lastLogin: Timestamp): Profile = {
        _lastLogin = lastLogin
        this
    }

    @QuerySqlField(name = "is_active", notNull = true)
    private var _isActive: Boolean = false
    def isActive: Boolean = _isActive
    def setActive: Profile = {
        _isActive = true
        this
    }
    def setInactive: Profile = {
        _isActive = false
        this
    }

    @QuerySqlField(name = "user_id", notNull = true)
    private var _userId: UUID = null

    private var _relatedUser: Option[User] = None
    def relatedUser: Option[User] = _relatedUser
    def setRelatedUser(user: User): Profile = {
        _relatedUser = Some(user)
        _userId = user.id
        this
    }

    @QuerySqlField(name = "organization_id", notNull = true)
    private var _organizationId: UUID = null

    private var _relatedOrganization: Option[Organization] = None
    def relatedOrganization: Option[Organization] = _relatedOrganization
    def setRelatedOrganization(organization: Organization): Profile = {
        _relatedOrganization = Some(organization)
        _organizationId = organization.id
        this
    }

    def persist(implicit ec: ExecutionContext): Future[Unit] = {
        this.setUpdatedAt(Timestamp.from(Instant.now()))
        store.persistProfile(this)
    }

    def remove(implicit ec: ExecutionContext): Future[Unit] = {
        store.deleteProfile(this)
    }
}
