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
import java.sql.Timestamp

class UserStore(implicit wrapper: IgniteClientNodeWrapper) extends SqlStore[UUID, User] {

    override val schema: String = "FUSION"
    override val cache: String = s"SQL_${schema}_USER"
    override protected var igniteCache: IgniteCache[UUID, User] = null

    super .init()

    // Create and get new User Object
    def makeUser: User = {
        implicit val userStore: UserStore = this
        new User
    }

    def getUserById(id: String): Future[User] = {
        executeQuery(
            makeQuery(
                "SELECT USER.id, username, password, PROFILE.id, lastname, firstname, last_login " +
                s"FROM $schema.USER as USER " +
                s"INNER JOIN $schema.PROFILE as PROFILE ON PROFILE.user_id = USER.id " +
                "WHERE id = ?")
            .setParams(List(id))
        ).transformWith({
            case Success(userResults) => {
                var row = userResults(0)
                Future.successful(
                    (for (
                        user <- Right(
                            makeUser
                            .setId(row(0).toString)
                            .setUsername(row(1).toString)
                            .setPassword(row(2).toString)
                        ) flatMap { user => 
                            if (row(3) != null && row(4) != null && row(5) != null && row(6) != null)
                                Right(user.setRelatedProfile(
                                    new ProfileStore().makeProfile
                                    .setId(row(3).toString)
                                    .setLastname(row(4).toString)
                                    .setFirstname(row(5).toString)
                                    .setLastLogin(row(6) match {
                                        case lastlogin: Timestamp => lastlogin.toInstant
                                        case _ => null
                                    })
                                ))
                            else Right(user)
                        }
                    ) yield user)
                    .getOrElse(null))
            }
            case Failure(cause) => Future.failed(cause)
        })
    }

    // Get existing users from database
    def getUsers(ids: List[String]): Future[List[User]] = {
        var queryString: String = 
            "SELECT USER.id, username, password, PROFILE.id, lastname, firstname, last_login " +
            s"FROM $schema.USER as USER " +
            s"INNER JOIN $schema.PROFILE as PROFILE ON PROFILE.user_id = USER.id " +
            "ORDER BY USER.id WHERE id in "
        var queryArgs: List[String] = ids
        queryString += s"(${(for (i <- 1 to queryArgs.length) yield "?").mkString(",")})"
        executeQuery(
            makeQuery(queryString)
            .setParams(queryArgs)
        ).transformWith({
            case Success(userResults) => {
                var users = userResults.par map(row => {
                    (for (
                        user <- Right(
                            makeUser
                            .setId(row(0).toString)
                            .setUsername(row(1).toString)
                            .setPassword(row(2).toString)
                        ) flatMap { user => 
                            if (row(3) != null && row(4) != null && row(5) != null && row(6) != null)
                                Right(user.setRelatedProfile(
                                    new ProfileStore().makeProfile
                                    .setId(row(3).toString)
                                    .setLastname(row(4).toString)
                                    .setFirstname(row(5).toString)
                                    .setLastLogin(row(6) match {
                                        case lastlogin: Timestamp => lastlogin.toInstant
                                        case _ => null
                                    })
                                ))
                            else Right(user)
                        }
                    ) yield user)
                    .getOrElse(null)
                })
                Future.successful(users.toList)
            }
            case Failure(cause) => throw cause
        })
    }

    // Save user object's modification to database
    def persistUser(user: User): Future[Boolean] = {
        executeQuery(
            makeQuery(s"MERGE INTO $schema.USER (id, username, password) values (?,?,?)")
            .setParams(List(user.id, user.username, user.password))
        ).transformWith({
            case Success(result) => 
                // result(0)(0) is the count of updated rows
                if (result(0)(0).toString.toInt == 1)
                    Future.successful(true)
                else
                    Future.successful(false)
            case Failure(cause) => Future.failed(cause)
        })
    }

    // Save several object's modifications
    def bulkPersistUsers(users: List[User]): Future[Int] = {
        var queryString: String = s"MERGE INTO $schema.USER (id, username, password) values "
        var queryArgs: List[List[_]] = users.map(user => {
            List(user.id, user.username, user.password)
        })
        queryString += (for (i <- 1 to queryArgs.length) yield "(?,?,?)").mkString(",")
        executeQuery(
            makeQuery(queryString)
            .setParams(queryArgs.flatten)
        ).transformWith({
            // case Success(result) => wrapper.getLogger().info(s"Updated rows : ${result(0)(0).toString()}")
            // result(0)(0) is the count of updated rows
            case Success(result) => Future.successful(result(0)(0).toString.toInt)
            case Failure(cause) => Future.failed(cause)
        })
    }

    // Remove user from database
    def removeUser(user: User): Future[Boolean] = {
        executeQuery(
            makeQuery(s"DELETE FROM $schema.USER WHERE id = ?")
            .setParams(List(user.id))
        ).transformWith({
            // case Success(result) => wrapper.getLogger().info(s"Deleted rows : ${result(0)(0).toString()}")
            case Success(result) =>
                if (result(0)(0).toString.toInt == 1)
                    Future.successful(true)
                else
                    Future.successful(false)
            case Failure(cause) =>  Future.failed(cause)
        })
    }

    // Remove several users from database
    def bulkRemoveUsers(users: List[User]): Future[Int] = {
        var queryString: String = s"DELETE FROM $schema.USER WHERE id in "
        var conditionArgs: List[String] = users.map(user => {
            user.id.toString
        })
        queryString += s"(${(for (i <- 1 to conditionArgs.length) yield "?").mkString(",")})"
        executeQuery(makeQuery(queryString)).transformWith({
            // case Success(result) => wrapper.getLogger().info(s"Deleted rows : ${result(0)(0).toString()}")
            case Success(result) => Future.successful(result(0)(0).toString.toInt)
            case Failure(cause) => Future.failed(cause)
        })
    }
}