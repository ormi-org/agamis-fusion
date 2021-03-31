package io.ogdt.fusion.core.db.datastore.models

import io.ogdt.fusion.db.drivers.typed.SqlStore
import io.ogdt.fusion.db.drivers.typed.SqlStore.Model

import io.ogdt.fusion.core.db.ignite.IgniteClientNodeWrapper

import org.apache.ignite.IgniteCache

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

import org.slf4j.LoggerFactory
import scala.collection.parallel.mutable.ParArray

class User extends Model with Serializable {

    private var _id: Long = -1
    def id: Long = _id
    def setId(id: Long): User = {
        _id = id
        this
    }

    private var _firstname: String = null
    def firstname: String = _firstname
    def setFirstname(firstname: String): User = {
        _firstname = firstname
        this
    }

    private var _lastname: String = null
    def lastname: String = _lastname
    def setLastname(lastname: String): User = {
        _lastname = lastname
        this
    }
}

class UserStore(wrapper: IgniteClientNodeWrapper) extends SqlStore[User](wrapper: IgniteClientNodeWrapper) {

    override val schema: String = "FUSION"
    override val cache: String = s"SQL_${schema}_USER"
    override val igniteCache: IgniteCache[Long,User] = wrapper.getCache[Long, User](cache)

    // val log = LoggerFactory.getLogger("io.ogdt.fusion.fs")

    def makeUser(): User = new User

    def getUsers(identifiers: Array[String]): Unit = {
        executeQuery(makeQuery(s"SELECT id, firstname, lastname FROM $schema.USER ORDER BY id")).onComplete({
            case Success(userList) => {
                var users = userList.par map(item => {
                    new User()
                        .setId(item(0).toString().toLong)
                        .setFirstname(item(1).toString())
                        .setLastname(item(2).toString())
                })
                log.info(users.toString())
            }
            case Failure(exception) => {
                throw exception
            }
        })
    }

    def persistUser(user: User) {

    }

    def removeUser(user: User) {

    }
}