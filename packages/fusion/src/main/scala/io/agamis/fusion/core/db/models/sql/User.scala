package io.agamis.fusion.core.db.models.sql

import io.agamis.fusion.core.db.common
import io.agamis.fusion.core.db.datastores.sql.UserStore
import io.agamis.fusion.core.db.models.sql.generics.exceptions.RelationNotFoundException
import io.agamis.fusion.core.db.models.sql.typed.Model
import org.apache.ignite.cache.query.annotations.QuerySqlField
import org.apache.ignite.transactions.Transaction

import java.sql.Timestamp
import java.time.Instant
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

class User(implicit @transient protected var store: UserStore) extends Model {

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

    def persist(implicit ec: ExecutionContext): Future[(Transaction, User)] = {
        this.setUpdatedAt(Timestamp.from(Instant.now()))
        store.persistUser(this).transformWith({
            case Success(tx) => Future.successful(tx)
            case Failure(e) => Future.failed(e)
        })
    }

    def remove(implicit ec: ExecutionContext): Future[(Transaction, User)] = {
        store.deleteUser(this).transformWith({
            case Success(tx) => Future.successful(tx)
            case Failure(e) => Future.failed(e)
        })
    }

    def authenticate(plainPassword: String): Boolean = {
        Security.validate(plainPassword, this._password)
    }

    def setStore(store: UserStore): User = {
        this.store = store
        this
    }

    // init transient fields for preventing null values
    def readResolve() = {
        this._relatedProfiles = List()
        this
    }

    object Security {
        import common.Security.SecurePasswordHashing

        def hash(plainPassword: String): String = {
            SecurePasswordHashing.hashPassword(plainPassword)
        }

        def validate(plainPassword: String, currentPasswordHash: String): Boolean = {
            SecurePasswordHashing.validatePassword(plainPassword, currentPasswordHash)
        }
    }
}