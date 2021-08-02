package io.ogdt.fusion.core.db.models.sql

import io.ogdt.fusion.core.db.datastores.sql.UserStore
import io.ogdt.fusion.core.db.models.sql.typed.Model

import org.apache.ignite.cache.query.annotations.QuerySqlField

import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import java.sql.Timestamp
import java.beans.Transient

import io.ogdt.fusion.core.db.models.sql.generics.exceptions.RelationAlreadyExistsException
import io.ogdt.fusion.core.db.models.sql.generics.exceptions.RelationNotFoundException

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
        _password = Security.hash(password)
        this
    }
    def setPasswordHash(passwordHash: String): User = {
        _password = passwordHash
        this
    }

    @transient
    private var _relatedProfiles: List[(Boolean, Profile)] = List()
    def relatedProfiles: List[(Boolean, Profile)] = _relatedProfiles
    def addRelatedProfile(profile: Profile): User = {
        _relatedProfiles.indexWhere(_._2.id == profile.id) match {
            case -1 => _relatedProfiles ::= (true, profile)
            case index => _relatedProfiles = _relatedProfiles.updated(index, _relatedProfiles(index).copy(_1 = true))
        }
        this
    }
    def removeRelatedProfile(profile: Profile): User = {
        _relatedProfiles.indexWhere(_._2.id == profile.id) match {
            case -1 => throw new RelationNotFoundException()
            case index => _relatedProfiles = _relatedProfiles.updated(index, _relatedProfiles(index).copy(_1 = false))
        }
        this
    }

    def persist(implicit ec: ExecutionContext): Future[Unit] = {
        store.persistUser(this)
    }

    def remove(implicit ec: ExecutionContext): Future[Unit] = {
        store.deleteUser(this)
    }

    def authenticate(plainPassword: String): Boolean = {
        Security.validate(plainPassword, this._password)
    }

    object Security {

        def hash(plainPassword: String): String = {
            // Implement password hashing
            "passwordHash"
        }

        def validate(plainPassword: String, currentPasswordHash: String): Boolean = {
            // Implement password validation
            true
        }
    }
}