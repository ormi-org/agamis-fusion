package io.ogdt.fusion.core.db.datastores.sql

import io.ogdt.fusion.core.db.models.sql.User
import io.ogdt.fusion.core.db.datastores.typed.SqlStore
import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper

import org.apache.ignite.IgniteCache

import scala.util.Success
import scala.util.Failure
import scala.concurrent.ExecutionContext.Implicits.global

import java.util.UUID
import scala.concurrent.Future

class UserStore(implicit wrapper: IgniteClientNodeWrapper) extends SqlStore[UUID, User] {

    override val schema: String = "FUSION"
    override val cache: String = s"SQL_${schema}_USER"
    override protected var igniteCache: IgniteCache[UUID, User] = null

    super .init()

    // Create and get new User Object
    def makeUser(): User = new User(this)

    // Get existing user from database
    def getUsers(identifiers: Array[String]): Future[List[User]] = {
        executeQuery(makeQuery(s"SELECT id, username, password FROM $schema.USER ORDER BY id")).transformWith({
            case Success(userList) => {
                var users = userList.par map(item => {
                    new User(this)
                        .setId(item(0).toString())
                        .setUsername(item(1).toString())
                        .setPassword(item(2).toString())
                })
                wrapper.getLogger().info(users.toString())
                Future.successful(users.toList)
            }
            case Failure(cause) => throw cause
        })
    }

    // Save user object's modification to database
    def persistUser(user: User) = {
        executeQuery(makeQuery(s"MERGE INTO $schema.USER (id, username, password) values ('${user.id}','${user.username}','${user.password}')")).onComplete({
            case Success(result) => wrapper.getLogger().info(s"Updated rows : ${result(0)(0).toString()}")
            case Failure(cause) => throw cause
        })
    }

    // Save several object's modifications
    def bulkPersistUsers(users: Vector[User]) = {
        var queryString: String = s"MERGE INTO $schema.USER (id, username, password) values "
        var queryArgs: Vector[String] = Vector()
        users.foreach(user => {
            queryArgs :+= s"(${user.id},'${user.username}','${user.password}')"
        })
        queryString += queryArgs.mkString(",")
        executeQuery(makeQuery(queryString)).onComplete({
            case Success(result) => wrapper.getLogger().info(s"Updated rows : ${result(0)(0).toString()}")
            case Failure(cause) => throw cause
        })
    }

    // Remove user from database
    def removeUser(user: User) {
        executeQuery(makeQuery(s"DELETE FROM $schema.USER WHERE id = '${user.id}'")).onComplete({
            case Success(result) => wrapper.getLogger().info(s"Deleted rows : ${result(0)(0).toString()}")
            case Failure(cause) =>  throw cause
        })
    }

    // Remove several users from database
    def bulkRemoveUsers(users: Vector[User]) = {
        var queryString: String = s"DELETE FROM $schema.USER WHERE "
        var conditionArgs: Vector[String] = Vector()
        users.foreach(user => {
            conditionArgs :+= s"id = '${user.id}'"
        })
        queryString += conditionArgs.mkString(" OR ")
        executeQuery(makeQuery(queryString)).onComplete({
            case Success(result) => wrapper.getLogger().info(s"Deleted rows : ${result(0)(0).toString()}")
            case Failure(cause) => throw cause
        })
    }
}