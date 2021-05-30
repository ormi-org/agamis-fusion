package io.ogdt.fusion.core.db.datastores.sql

import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper

import io.ogdt.fusion.core.db.datastores.typed.SqlMutableStore
import io.ogdt.fusion.core.db.datastores.typed.sql.SqlStoreQuery
import io.ogdt.fusion.core.db.datastores.typed.sql.GetEntityFilters

import io.ogdt.fusion.core.db.common.Utils

import io.ogdt.fusion.core.db.datastores.sql.exceptions.users.{
    UserNotFoundException,
    UserNotPersistedException,
    DuplicateUserException,
    UserQueryExecutionException
}
import io.ogdt.fusion.core.db.datastores.sql.exceptions.NoEntryException

import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.cache.CacheAtomicityMode

import scala.util.Success
import scala.util.Failure
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

import java.sql.Timestamp
import java.util.UUID

import scala.jdk.CollectionConverters._

import scala.collection.mutable.ListBuffer

import io.ogdt.fusion.core.db.models.sql.User

class UserStore(implicit wrapper: IgniteClientNodeWrapper) extends SqlMutableStore[UUID, User] {

    override val schema: String = "FUSION"
    override val cache: String = s"SQL_${schema}_USER"
    override protected var igniteCache: IgniteCache[UUID, User] = wrapper.cacheExists(cache) match {
        case true => wrapper.getCache[UUID, User](cache)
        case false => {
            wrapper.createCache[UUID, User](
                wrapper.makeCacheConfig[UUID, User]
                .setCacheMode(CacheMode.REPLICATED)
                .setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL)
                .setDataRegionName("Fusion")
                .setName(cache)
                .setSqlSchema(schema)
                .setIndexedTypes(classOf[UUID], classOf[User])
            )
        }
    }

    // Create and get new User Object
    def makeUser: User = {
        implicit val userStore: UserStore = this
        new User
    }

    def makeUsersQuery(queryFilters: UserStore.GetUsersFilters): SqlStoreQuery = {
        var queryString: String = 
            "SELECT USER.id, username, password, USER.created_at, USER.updated_at, " +
            "PROFILE.id, lastname, firstname, last_login, is_active, PROFILE.created_at, PROFILE.updated_at " +
            s"FROM $schema.USER as USER " +
            s"INNER JOIN $schema.PROFILE as PROFILE ON PROFILE.user_id = USER.id"
        var queryArgs: ListBuffer[String] = ListBuffer()
        var whereStatements: ListBuffer[String] = ListBuffer()
        queryFilters.filters.foreach({ filter =>
            var innerWhereStatement: ListBuffer[String] = ListBuffer()
            // manage ids search
            if (filter.id.length > 0) {
                innerWhereStatement += s"USER.id in (${(for (i <- 1 to filter.id.length) yield "?").mkString(",")})"
                queryArgs ++= filter.id
            }
            // manage usernames search
            if (filter.username.length > 0) {
                innerWhereStatement += s"USER.username in (${(for (i <- 1 to filter.username.length) yield "?").mkString(",")})"
                queryArgs ++= filter.username
            }
            // manage metadate search
            filter.createdAt match {
                case Some((test, time)) => {
                    innerWhereStatement += s"USER.created_at ${
                        test match {
                            case "eq" => "="
                            case "gt" => ">"
                            case "lt" => "<"
                            case "neq" => "<>"
                        }
                    } ?"
                    queryArgs += time.toString
                }
                case None => ()
            }
            filter.updatedAt match {
                case Some((test, time)) => {
                    innerWhereStatement += s"USER.updated_at ${
                        test match {
                            case "eq" => "="
                            case "gt" => ">"
                            case "lt" => "<"
                            case "neq" => "<>"
                        }
                    } ?"
                    queryArgs += time.toString
                }
                case None => ()
            }
            whereStatements += innerWhereStatement.mkString(" AND ")
        })
        // compile whereStatements
        if (whereStatements.length > 0) {
            queryString += " WHERE " + whereStatements.reverse.mkString(" OR ")
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
        makeQuery(queryString)
        .setParams(queryArgs.toList)
    }

    // Get existing users from database
    def getUsers(queryFilters: UserStore.GetUsersFilters)(implicit ec: ExecutionContext): Future[List[User]] = {
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
                            .setCreatedAt(entityReflection._2(0)(3) match {
                                case createdAt: Timestamp => createdAt
                                case _ => null
                            })
                            .setUpdatedAt(entityReflection._2(0)(4) match {
                                case updatedAt: Timestamp => updatedAt
                                case _ => null
                            })
                        ) flatMap { user => // pass created user to profile mapping step
                            // filter entities to exclude results where compulsory fields are missing or set to NULL
                            entityReflection._2.filter(
                                (row: List[_]) => (row(5) != null && row(6) != null && row(7) != null && row(8) != null && row(9) != null && row(10) != null && row(11) != null)
                            ).collect(row => { // collect inner List to transform it to profile objects
                                ( for(
                                    profile <- Right(
                                        new ProfileStore().makeProfile
                                        .setId(row(5).toString)
                                        .setLastname(row(6).toString)
                                        .setFirstname(row(7).toString)
                                        .setLastLogin(row(8) match {
                                            case lastlogin: Timestamp => lastlogin
                                            case _ => null
                                        })
                                        .setCreatedAt(row(10) match {
                                            case createdAt: Timestamp => createdAt
                                            case _ => null
                                        })
                                        .setUpdatedAt(row(11) match {
                                            case updatedAt: Timestamp => updatedAt
                                            case _ => null
                                        })
                                    ) flatMap { profile =>
                                        row(9) match {
                                            case isActive: Boolean => {
                                                if (isActive) Right(profile.setActive)
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
            case Failure(cause) => Future.failed(UserQueryExecutionException(cause))
        })
    }

    def getAllUsers(implicit ec: ExecutionContext): Future[List[User]] = {
        getUsers(UserStore.GetUsersFilters.none).transformWith({
            case Success(users) => 
                users.length match {
                    case 0 => Future.failed(new NoEntryException("User table is empty"))
                    case _ => Future.successful(users)
                }
            case Failure(cause) => Future.failed(cause)
        })
    }

    def getUserById(id: String)(implicit ec: ExecutionContext): Future[User] = {
        getUsers(
            UserStore.GetUsersFilters(
                List(
                    UserStore.GetUsersFilter(
                        List(id),
                        List(),
                        None,
                        None
                    )
                ),
                List()
            )
        ).transformWith({
            case Success(users) =>
                users.length match {
                    case 0 => Future.failed(new UserNotFoundException(s"User ${id} couldn't be found"))
                    case 1 => Future.successful(users(0))
                    case _ => Future.failed(new DuplicateUserException)
                }
            case Failure(cause) => Future.failed(cause)
        })
    }

    def getUserByUsername(username: String)(implicit ec: ExecutionContext): Future[User] = {
        getUsers(
            UserStore.GetUsersFilters(
                List(
                    UserStore.GetUsersFilter(
                        List(),
                        List(username),
                        None,
                        None
                    )
                ),
                List()
            )
        ).transformWith({
            case Success(users) =>
                users.length match {
                    case 0 => Future.failed(new UserNotFoundException(s"User ${username} couldn't be found"))
                    case 1 => Future.successful(users(0))
                    case _ => Future.failed(new DuplicateUserException)
                }
            case Failure(cause) => Future.failed(cause)
        })
    }

    // Save user object's modification to database
    def persistUser(user: User)(implicit ec: ExecutionContext): Future[Unit] = {
        Utils.igniteToScalaFuture(igniteCache.putAsync(
            user.id, user
        )).transformWith({
            case Success(value) => Future.unit
            case Failure(cause) => Future.failed(UserNotPersistedException(cause))
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
    def bulkPersistUsers(users: List[User])(implicit ec: ExecutionContext): Future[BulkPersistUsersResult] = {
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
            case Failure(cause) => Future.failed(UserNotPersistedException(cause))
        })
    }

    // Delete user from database
    def deleteUser(user: User)(implicit ec: ExecutionContext): Future[Unit] = {
        Utils.igniteToScalaFuture(igniteCache.removeAsync(user.id))
        .transformWith({
            case Success(value) => Future.unit
            case Failure(cause) => Future.failed(UserNotPersistedException(cause))
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
    def bulkDeleteUsers(users: List[User])(implicit ec: ExecutionContext): Future[BulkDeleteUsersResult] = {
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
            case Failure(cause) => Future.failed(UserNotPersistedException(cause))
        })
    }
}

object UserStore {
    case class GetUsersFilter(
        id: List[String],
        username: List[String],
        createdAt: Option[(String, Timestamp)], // (date, (eq, lt, gt, ne))
        updatedAt: Option[(String, Timestamp)], // (date, (eq, lt, gt, ne))
    )
    case class GetUsersFilters(
        filters: List[GetUsersFilter],
        orderBy: List[(String, Int)] // (column, direction)
    ) extends GetEntityFilters

    object GetUsersFilters {
        def none: GetUsersFilters = {
            GetUsersFilters(
                List(),
                List()
            )
        }
    }
}