package io.ogdt.fusion.core.db.datastores.sql

import io.ogdt.fusion.core.db.models.sql.User
import io.ogdt.fusion.core.db.datastores.typed.SqlStore
import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper

import org.apache.ignite.IgniteCache

import scala.util.Success
import scala.util.Failure
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future
import java.sql.Timestamp
import java.util.UUID
import scala.jdk.CollectionConverters._
import io.ogdt.fusion.core.db.common.Utils
import io.ogdt.fusion.core.db.datastores.typed.sql.SqlStoreQuery

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
                "SELECT USER.id, username, password, " +
                "PROFILE.id, lastname, firstname, last_login, is_active " +
                s"FROM $schema.USER as USER " +
                s"INNER JOIN $schema.PROFILE as PROFILE ON PROFILE.user_id = USER.id " +
                "WHERE USER.id = ?")
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
                            userResults.foreach(row => {
                                if (row(3) != null && row(4) != null && row(5) != null && row(6) != null && row(7) != null)
                                user.addRelatedProfile(
                                    ( for(
                                        profile <- Right(
                                            new ProfileStore().makeProfile
                                            .setId(row(3).toString)
                                            .setLastname(row(4).toString)
                                            .setFirstname(row(5).toString)
                                            .setLastLogin(row(6) match {
                                                case lastlogin: Timestamp => lastlogin
                                                case _ => null
                                            })
                                        ) flatMap { profile =>
                                            row(7) match {
                                                case isActive: Boolean => {
                                                    if(isActive) Right(profile.setActive)
                                                    else Right(profile.setInactive)
                                                }
                                                case _ => Right(profile)
                                            }
                                        }
                                    ) yield profile)
                                    .getOrElse(null)
                                )
                            })
                            Right(user)
                        }
                    ) yield user)
                    .getOrElse(null))
            }
            case Failure(cause) => Future.failed(cause)
        })
    }

    def makeUsersQuery(queryFilters: UserStore.GetUsersFilters): SqlStoreQuery = {
        var queryString: String = 
            "SELECT USER.id, username, password, " +
            "PROFILE.id, lastname, firstname, last_login, is_active " +
            s"FROM $schema.USER as USER " +
            s"INNER JOIN $schema.PROFILE as PROFILE ON PROFILE.user_id = USER.id"
        var queryArgs: List[String] = List()
        var whereStatements: List[String] = List()
        // manage ids search
        if (queryFilters.ids.length > 0) {
            whereStatements = s"USER.id in (${(for (i <- 1 to queryFilters.ids.length) yield "?").mkString(",")})" :: whereStatements
            queryArgs = queryArgs ::: queryFilters.ids
        }
        // manage usernames search
        if (queryFilters.usernames.length > 0) {
            whereStatements = s"USER.username in (${(for (i <- 1 to queryFilters.usernames.length) yield "?").mkString(",")})" :: whereStatements
            queryArgs = queryArgs ::: queryFilters.usernames
        }
        if (whereStatements.length > 0) {
            queryString += " WHERE " + whereStatements.reverse.mkString(" AND ")
        }
        // manage order
        if (queryFilters.orderBy.length > 0) {
            queryString += s" ORDER BY ${queryFilters.orderBy.map( o =>
                s"USER.${o._1} ${o._2 match {
                    case 1 => "ASC"
                    case -1 => "DESC"
                }}"
            ).mkString(", ")}"
        }
        println(queryArgs)
        makeQuery(queryString)
        .setParams(queryArgs)
    }

    // Get existing users from database
    def getUsers(queryFilters: UserStore.GetUsersFilters): Future[List[User]] = {
        executeQuery(makeUsersQuery(queryFilters)).transformWith({
            case Success(userResults) => {
                // map each user from queryResult by grouping results by USER.id and mapping to user objects creation
                var users = userResults.toList.groupBy(_(0)).map(entityReflection => {
                    (for (
                        // Start a for comprehension
                        user <- Right(makeUser
                            .setId(entityReflection._2(0)(0).toString)
                            .setUsername(entityReflection._2(0)(1).toString)
                            .setPassword(entityReflection._2(0)(2).toString)
                        ) flatMap { user => // pass created user to profile mapping step
                            // filter entities to exclude results where compulsory fields are missing or set to NULL
                            entityReflection._2.filter((row: List[_]) => (row(3) != null && row(4) != null && row(5) != null && row(6) != null && row(7) != null))
                            .collect(row => { // collect inner List to transform it to profile objects
                                ( for(
                                    profile <- Right(
                                        new ProfileStore().makeProfile
                                        .setId(row(3).toString)
                                        .setLastname(row(4).toString)
                                        .setFirstname(row(5).toString)
                                        .setLastLogin(row(6) match {
                                            case lastlogin: Timestamp => lastlogin
                                            case _ => null
                                        })
                                    ) flatMap { profile =>
                                        row(7) match {
                                            case isActive: Boolean => {
                                                if(isActive) Right(profile.setActive)
                                                else Right(profile.setInactive)
                                            }
                                            case _ => Right(profile)
                                        }
                                    }
                                ) yield profile)
                                .getOrElse(null)
                            }).foreach(profile => {
                                // Add each valid profiles to user
                                user.addRelatedProfile(profile)
                            })
                            Right(user)
                        }
                    ) yield user).getOrElse(null)                   
                })
                Future.successful(users.toList)
            }
            case Failure(cause) => throw cause
        })
    }

    // Save user object's modification to database
    def persistUser(user: User): Future[Unit] = {
        Utils.igniteToScalaFuture(igniteCache.putAsync(
            user.id, user
        )).transformWith({
            case Success(value) => Future.successful()
            case Failure(cause) => Future.failed(cause)
        })
    }

    /** A result of bulkPersistUsers method
      * 
      * @constructor create a new BulkPersistUsersResult with a count of inserted Users and a list of errors
      * @param inserts a count of the effectively inserted Users
      * @param errors a list of errors catched from a user insertion
      */
    case class BulkPersistUsersResult(inserts: Int, errors: List[String])

    // Save several object's modifications
    def bulkPersistUsers(users: List[User]): Future[BulkPersistUsersResult] = {
        Utils.igniteToScalaFuture(igniteCache.putAllAsync(
            (users.map(_.id) zip users).toMap[UUID, User].asJava
        )).transformWith({
            case Success(value) => {
                Future.sequence(
                    users.map(user => Utils.igniteToScalaFuture(igniteCache.containsKeyAsync(user.id)))
                ).map(lookup => (users zip lookup))
                .transformWith(lookup => {
                    Future.successful(BulkPersistUsersResult(
                        lookup.get.filter(_._2 == true).length,
                        lookup.get.filter(_._2 == false).map("Insert user "+_._1.toString+" failed")
                    ))
                })
            }
            case Failure(cause) => Future.failed(cause)
        })
    }

    // Delete user from database
    def deleteUser(user: User): Future[Unit] = {
        
        Utils.igniteToScalaFuture(igniteCache.removeAsync(user.id))
        .transformWith({
            case Success(value) => Future.successful()
            case Failure(cause) => Future.failed(cause)
        })
    }

    /** A result of bulkDeleteUsers method
      * 
      * @constructor create a new BulkDeleteUsersResult with a count of deleted Users and a list of errors
      * @param inserts a count of the effectively deleted Users
      * @param errors a list of errors catched from a user deletion
      */
    case class BulkDeleteUsersResult(inserts: Int, errors: List[String])

    // Delete several users from database
    def bulkDeleteUsers(users: List[User]): Future[BulkDeleteUsersResult] = {
        Utils.igniteToScalaFuture(igniteCache.removeAllAsync(users.map(_.id).toSet.asJava))
        .transformWith({
            case Success(value) => {
                Future.sequence(
                    users.map(user => Utils.igniteToScalaFuture(igniteCache.containsKeyAsync(user.id)))
                ).map(lookup => (users zip lookup))
                .transformWith(lookup => {
                    Future.successful(BulkDeleteUsersResult(
                        lookup.get.filter(_._2 == false).length,
                        lookup.get.filter(_._2 == true).map("Failed to delete user "+_._1.toString)
                    ))
                })
            }
            case Failure(cause) => Future.failed(cause)
        })
    }
}

object UserStore {
    case class GetUsersFilters(
        ids: List[String],
        usernames: List[String],
        orderBy: List[(String, Int)] // (column, direction)
    ) {
        def ordered: Boolean = orderBy.length > 0
    }
}