package io.ogdt.fusion.core.db.datastores.sql

import io.ogdt.fusion.core.db.models.sql.User
import io.ogdt.fusion.core.db.datastores.typed.SqlStore
import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper

import org.apache.ignite.IgniteCache

import scala.util.Success
import scala.util.Failure
import scala.concurrent.ExecutionContext.Implicits.global

import java.util.UUID

class UserStore(wrapper: IgniteClientNodeWrapper) extends SqlStore[UUID, User](wrapper: IgniteClientNodeWrapper) {

    override val schema: String = "FUSION"
    override val cache: String = s"SQL_${schema}_USER"
    override var igniteCache: IgniteCache[UUID, User] = null

    super .init()

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
                wrapper.getLogger().info(users.toString())
            }
            case Failure(exception) => {
                throw exception
            }
        })
    }

    def persistUser(user: User) = {
        executeQuery(makeQuery(s"MERGE INTO $schema.USER (id, username, password) values ('${user.id}','${user.username}','${user.password}')")).onComplete({
            case Success(result) => {
                wrapper.getLogger().info(s"Updated rows : ${result(0)(0).toString()}")
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
                wrapper.getLogger().info(s"Updated rows : ${result(0)(0).toString()}")
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