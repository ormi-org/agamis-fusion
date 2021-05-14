package io.ogdt.fusion.core.db.models.sql

import io.ogdt.fusion.core.db.datastores.sql.UserStore
import io.ogdt.fusion.core.db.models.sql.typed.Model

import org.apache.ignite.cache.query.annotations.QuerySqlField

import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import java.sql.Timestamp
import java.beans.Transient

class User(implicit @transient protected val store: UserStore) extends Model {

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

    @transient
    private var _relatedProfiles: List[Profile] = List()
    def relatedProfiles: List[Profile] = _relatedProfiles
    def addRelatedProfile(profile: Profile): User = {
        _relatedProfiles ::= profile
        this
    }
    def removeRelatedProfile(profile: Profile): User = {
        _relatedProfiles = _relatedProfiles.filterNot(p => p.id == profile.id)
        this
    }

    def persist(implicit ec: ExecutionContext): Future[Unit] = {
        store.persistUser(this)
    }

    def remove(implicit ec: ExecutionContext): Future[Unit] = {
        store.deleteUser(this)
    }
}