package io.ogdt.fusion.core.db.datastore.models

import io.ogdt.fusion.db.drivers.typed.SqlStore
import io.ogdt.fusion.db.drivers.typed.SqlStore.Model

import io.ogdt.fusion.core.db.ignite.IgniteClientNodeWrapper

import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.query.annotations.QuerySqlField

import java.util.UUID

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

import org.slf4j.LoggerFactory
import scala.collection.parallel.mutable.ParArray
import scala.collection.parallel.immutable.ParVector
import io.ogdt.fusion.env.EnvContainer
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.cache.query.SqlFieldsQuery
import scala.reflect.ClassTag

class User(protected val store: UserStore) extends Model {

    // generate UUID at User creation
    genId()

    private def genId() = {
        _id = UUID.randomUUID()
    }

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

class UserStore(wrapper: IgniteClientNodeWrapper) extends SqlStore[UUID, User](wrapper: IgniteClientNodeWrapper) {

    override implicit val kTag: ClassTag[UUID] = implicitly[ClassTag[UUID]]

    override implicit val mTag: ClassTag[User] = implicitly[ClassTag[User]]


    override val schema: String = "FUSION"
    override val cache: String = s"SQL_${schema}_USER"
    override var igniteCache: IgniteCache[UUID, User] = null

    def makeUser(): User = new User(this)

    def getUsers(identifiers: Array[String]): Unit = {
        executeQuery(makeQuery(s"SELECT id, username, password FROM $schema.USER ORDER BY id")).onComplete({
            case Success(userList) => {
                var users = userList.par map(item => {
                    new User(this)
                        .setId(item(0).toString())
                        .setUsername(item(1).toString())
                        .setPassword(item(2).toString())
                })
                log.info(users.toString())
            }
            case Failure(exception) => {
                throw exception
            }
        })
    }

    def persistUser(user: User) = {
        executeQuery(makeQuery(s"MERGE INTO $schema.USER (id, username, password) values ('${user.id}','${user.username}','${user.password}')")).onComplete({
            case Success(result) => {
                log.info(s"Updated rows : ${result(0)(0).toString()}")
            }
            case Failure(exception) => {
                throw exception
            }
        })
    }

    def bulkPersistUsers(users: Vector[User]) = {
        var queryString: String = s"MERGE INTO $schema.USER (id, username, password) values "
        var queryArgs: Vector[String] = Vector()
        users.foreach(user => {
            queryArgs :+= s"(${user.id},'${user.username}','${user.password}')"
        })
        queryString += queryArgs.mkString(",")
        executeQuery(makeQuery(queryString)).onComplete({
            case Success(result) => {
                log.info(s"Updated rows : ${result(0)(0).toString()}")
            }
            case Failure(exception) => {
                throw exception
            }
        })
    }

    def removeUser(user: User) {

    }
}

object UserStore {
    def fromWrapper(wrapper: IgniteClientNodeWrapper): Option[UserStore] = {
        Some(new UserStore(wrapper))
    }
}