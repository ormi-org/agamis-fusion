package io.agamis.fusion.core.db.datastores.sql

import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper

import io.agamis.fusion.core.db.datastores.typed.SqlMutableStore
import io.agamis.fusion.core.db.datastores.typed.sql.SqlStoreQuery
import io.agamis.fusion.core.db.datastores.typed.sql.GetEntityFilters

import io.agamis.fusion.core.db.common.Utils

import io.agamis.fusion.core.db.datastores.sql.exceptions.typed.users.{
  UserNotFoundException,
  UserNotPersistedException,
  DuplicateUserException,
  UserQueryExecutionException
}
import io.agamis.fusion.core.db.datastores.sql.exceptions.NoEntryException

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

import io.agamis.fusion.core.db.models.sql.User
import scala.util.Try
import io.agamis.fusion.core.db.models.sql.generics.Email
import io.agamis.fusion.core.db.datastores.sql.common.Filter
import io.agamis.fusion.core.db.datastores.sql.common.exceptions.InvalidComparisonOperatorException
import io.agamis.fusion.core.db.datastores.sql.common.exceptions.InvalidOrderingOperatorException
import java.{util => ju}
import io.agamis.fusion.core.db.datastores.sql.common.Placeholder
import io.agamis.fusion.core.db.datastores.sql.common.Pagination

class UserStore(implicit wrapper: IgniteClientNodeWrapper)
    extends SqlMutableStore[UUID, User] {

  override val schema: String = "FUSION"
  override val cache: String = s"SQL_${schema}_USER"
  override protected val igniteCache: IgniteCache[UUID, User] =
    if (wrapper.cacheExists(cache)) {
      wrapper.getCache[UUID, User](cache)
    } else {
      wrapper.createCache[UUID, User](
        wrapper
          .makeCacheConfig[UUID, User]
          .setCacheMode(CacheMode.REPLICATED)
          .setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL)
          .setDataRegionName("Fusion")
          .setName(cache)
          .setSqlSchema(schema)
          .setIndexedTypes(classOf[UUID], classOf[User])
      )
    }

  // Create and get new User Object
  def makeUser: User = {
    implicit val userStore: UserStore = this
    new User
  }

  def makeUsersQuery(queryFilters: UserStore.GetUsersFilters): SqlStoreQuery = {
    var baseQueryString = queryString.replace("$SCHEMA", schema)
    val queryArgs: ListBuffer[String] = ListBuffer()
    val whereStatements: ListBuffer[String] = ListBuffer()
    queryFilters.filters.foreach({ filter =>
      val innerWhereStatement: ListBuffer[String] = ListBuffer()
      // manage ids search
      if (filter.id.nonEmpty) {
        innerWhereStatement += s"${UserStore.Column.ID().name} in (${(for (_ <- 1 to filter.id.length)
          yield "?").mkString(",")})"
        queryArgs ++= filter.id
      }
      // manage usernames search
      if (filter.username.nonEmpty) {
        innerWhereStatement += s"${UserStore.Column.USERNAME().name} in (${(for (_ <- 1 to filter.username.length)
          yield "?").mkString(",")})"
        queryArgs ++= filter.username
      }
      // manage metadate search
      filter.createdAt match {
        case Some((test, time)) =>
          innerWhereStatement += s"${UserStore.Column.CREATED_AT().name} ${test match {
            case Filter.ComparisonOperator.Equal =>
              Filter.ComparisonOperator.SQL.Equal
            case Filter.ComparisonOperator.GreaterThan =>
              Filter.ComparisonOperator.SQL.GreaterThan
            case Filter.ComparisonOperator.LowerThan =>
              Filter.ComparisonOperator.SQL.LowerThan
            case Filter.ComparisonOperator.NotEqual =>
              Filter.ComparisonOperator.SQL.NotEqual
            case _ => throw InvalidComparisonOperatorException(test)
          }} ?"
          queryArgs += time.toString
        case None => ()
      }
      filter.updatedAt match {
        case Some((test, time)) =>
          innerWhereStatement += s"${UserStore.Column.UPDATED_AT().name} ${test match {
            case Filter.ComparisonOperator.Equal =>
              Filter.ComparisonOperator.SQL.Equal
            case Filter.ComparisonOperator.GreaterThan =>
              Filter.ComparisonOperator.SQL.GreaterThan
            case Filter.ComparisonOperator.LowerThan =>
              Filter.ComparisonOperator.SQL.LowerThan
            case Filter.ComparisonOperator.NotEqual =>
              Filter.ComparisonOperator.SQL.NotEqual
            case _ => throw InvalidComparisonOperatorException(test)
          }} ?"
          queryArgs += time.toString
        case None => ()
      }
      whereStatements += innerWhereStatement.mkString(" AND ")
    })
    // compile whereStatements
    baseQueryString.replace(
      Placeholder.WHERE_STATEMENT,
      whereStatements.nonEmpty match {
        case true  => " WHERE " + whereStatements.reverse.mkString(" OR ")
        case false => ""
      }
    )
    // manage order
    baseQueryString.replace(
      Placeholder.ORDER_BY_STATEMENT,
      whereStatements.nonEmpty match {
        case true =>
          s" ORDER BY ${queryFilters.orderBy
            .map(o =>
              s"u.${o._1} ${o._2 match {
                case Filter.OrderingOperators.Ascending =>
                  Filter.OrderingOperators.SQL.Ascending
                case Filter.OrderingOperators.Descending =>
                  Filter.OrderingOperators.SQL.Descending
                case _ => throw InvalidOrderingOperatorException(o._2)
              }}"
            )
            .mkString(", ")}"
        case false => ""
      }
    )
    baseQueryString.replace(
      Placeholder.PAGINATION,
      queryFilters.pagination match {
        case Some(p) => s" LIMIT ${p.limit} OFFSET ${p.offset} "
        case None    => s" LIMIT ${Pagination.Default.Limit} OFFSET ${Pagination.Default.Offset} "
      }
    )
    makeQuery(baseQueryString)
      .setParams(queryArgs.toList)
  }

  // Get existing users from database
  def getUsers(
      queryFilters: UserStore.GetUsersFilters
  )(implicit ec: ExecutionContext): Future[List[User]] = {
    executeQuery(makeUsersQuery(queryFilters)).transformWith({
      case Success(rows) =>
        // map each user from queryResult by grouping results by USER.id and mapping to user objects creation
        val entityReflections = rows.groupBy(_.head)
        // val users = rows.map({ row =>
        //   (for (
        //     user <- Right(
        //       makeUser
        //         .setId(row(UserStore.Column.ID().order))
        //     )
        //   ) yield user)
        // })
        val users = rows
          .map(_.head)
          .distinct
          .map(entityReflections(_))
          .map(entityReflection => {
            val groupedRows =
              getRelationsGroupedRowsFrom(entityReflection, 5, 6)
            groupedRows.get("USER") match {
              case Some(userReflections) =>
                val userDef = userReflections.head.head._2
                (for (
                  // Start a for comprehension
                  user <- Right(
                    makeUser
                      .setId(userDef(0))
                      .setUsername(userDef(1))
                      .setPassword(userDef(2))
                      .setCreatedAt(
                        Utils.timestampFromString(userDef(3)) match {
                          case createdAt: Timestamp => createdAt
                          case _                    => null
                        }
                      )
                      .setUpdatedAt(
                        Utils.timestampFromString(userDef(4)) match {
                          case updatedAt: Timestamp => updatedAt
                          case _                    => null
                        }
                      )
                  ) flatMap {
                    user => // pass created user to profile mapping step
                      groupedRows.get("PROFILE") match {
                        case Some(profileReflections) =>
                          //PROFILE.id, PROFILE.lastname, PROFILE.firstname, v, PROFILE.last_login, PROFILE.is_active, PROFILE.user_id, PROFILE.organization_id, PROFILE.created_at, PROFILE.updated_at
                          profileReflections.foreach({ profileReflection =>
                            val profileDef = profileReflection.head._2
                            for (
                              profile <- Right(
                                new ProfileStore().makeProfile
                                  .setId(profileDef(0))
                                  .setLastname(profileDef(1))
                                  .setFirstname(profileDef(2))
                                  .setLastLogin(
                                    Utils.timestampFromString(
                                      profileDef(4)
                                    ) match {
                                      case lastlogin: Timestamp => lastlogin
                                      case _                    => null
                                    }
                                  )
                                  .setRelatedUser(
                                    new UserStore().makeUser
                                      .setId(profileDef(6))
                                  )
                                  .setRelatedOrganization(
                                    new OrganizationStore().makeOrganization
                                      .setId(profileDef(7))
                                  )
                                  .setCreatedAt(
                                    Utils.timestampFromString(
                                      profileDef(8)
                                    ) match {
                                      case createdAt: Timestamp => createdAt
                                      case _                    => null
                                    }
                                  )
                                  .setUpdatedAt(
                                    Utils.timestampFromString(
                                      profileDef(9)
                                    ) match {
                                      case updatedAt: Timestamp => updatedAt
                                      case _                    => null
                                    }
                                  )
                              ) flatMap { profile =>
                                Try(profileDef(5).toBoolean) match {
                                  case Success(isActive) =>
                                    if (isActive) Right(profile.setActive())
                                    else Right(profile.setInactive())
                                  case Failure(_) => Right(profile)
                                }
                              } flatMap { profile =>
                                val profileMainEmailDef =
                                  profileDef(3).split(";")
                                profileMainEmailDef.length match {
                                  case 2 =>
                                    Right(
                                      profile.setMainEmail(
                                        Email.apply
                                          .setId(profileMainEmailDef(0))
                                          .setAddress(profileMainEmailDef(1))
                                      )
                                    )
                                  case _ => Right(profile)
                                }
                              }
                            ) yield user.addRelatedProfile(profile)
                          })
                        case None =>
                      }
                      Right(user)
                  }
                ) yield user).getOrElse(null)
              case None =>
            }
          })
        Future.successful(users.toList.asInstanceOf[List[User]])
      case Failure(cause) => Future.failed(UserQueryExecutionException(cause))
    })
  }

  def getAllUsers(implicit ec: ExecutionContext): Future[List[User]] = {
    getUsers(
      UserStore
        .GetUsersFilters()
        .copy(
          orderBy = List(
            (UserStore.Column.ID(), 1)
          )
        )
    ).transformWith({
      case Success(users) =>
        users.length match {
          case 0 => Future.failed(NoEntryException("User table is empty"))
          case _ => Future.successful(users)
        }
      case Failure(cause) => Future.failed(cause)
    })
  }

  def getUserById(id: String)(implicit ec: ExecutionContext): Future[User] = {
    getUsers(
      UserStore
        .GetUsersFilters()
        .copy(
          filters = List(
            UserStore
              .GetUsersFilter()
              .copy(
                id = List(id)
              )
          )
        )
    ).transformWith({
      case Success(users) =>
        users.length match {
          case 0 =>
            Future.failed(UserNotFoundException(s"User $id couldn't be found"))
          case 1 => Future.successful(users.head)
          case _ => Future.failed(new DuplicateUserException)
        }
      case Failure(cause) => Future.failed(cause)
    })
  }

  def getUserByUsername(
      username: String
  )(implicit ec: ExecutionContext): Future[User] = {
    getUsers(
      UserStore
        .GetUsersFilters()
        .copy(
          filters = List(
            UserStore
              .GetUsersFilter()
              .copy(
                username = List(username)
              )
          )
        )
    ).transformWith({
      case Success(users) =>
        users.length match {
          case 0 =>
            Future.failed(
              UserNotFoundException(s"User $username couldn't be found")
            )
          case 1 => Future.successful(users.head)
          case _ => Future.failed(new DuplicateUserException)
        }
      case Failure(cause) => Future.failed(cause)
    })
  }

  // Save user object's modification to database
  def persistUser(user: User)(implicit ec: ExecutionContext): Future[Unit] = {
    makeTransaction match {
      case Success(tx) =>
        Utils
          .igniteToScalaFuture(
            igniteCache.putAsync(
              user.id,
              user
            )
          )
          .transformWith({
            case Success(_) => Future.unit
            case Failure(cause) =>
              Future.failed(UserNotPersistedException(cause))
          })
      case Failure(cause) => Future.failed(cause)
    }
  }

  /** A result of bulkPersistUsers method
    *
    * @constructor
    *   create a new BulkPersistUsersResult with a count of inserted Users and a
    *   list of errors
    * @param inserts
    *   a count of the effectively inserted Users
    * @param errors
    *   a list of errors catched from a user insertion
    */
  case class BulkPersistUsersResult(inserts: Int, errors: List[String])

  // Save several object's modifications
  def bulkPersistUsers(
      users: List[User]
  )(implicit ec: ExecutionContext): Future[BulkPersistUsersResult] = {
    Utils
      .igniteToScalaFuture(
        igniteCache.putAllAsync(
          (users.map(_.id) zip users).toMap[UUID, User].asJava
        )
      )
      .transformWith({
        case Success(_) =>
          Future
            .sequence(
              users.map(user =>
                Utils.igniteToScalaFuture(igniteCache.containsKeyAsync(user.id))
              )
            )
            .map(lookup => users zip lookup)
            .transformWith(lookup => {
              Future.successful(
                BulkPersistUsersResult(
                  lookup.get.count(_._2 == true),
                  lookup.get
                    .filter(_._2 == false)
                    .map("Insert user " + _._1.toString + " failed")
                )
              )
            })
        case Failure(cause) => Future.failed(UserNotPersistedException(cause))
      })
  }

  // Delete user from database
  def deleteUser(user: User)(implicit ec: ExecutionContext): Future[Unit] = {
    Utils
      .igniteToScalaFuture(igniteCache.removeAsync(user.id))
      .transformWith({
        case Success(_)     => Future.unit
        case Failure(cause) => Future.failed(UserNotPersistedException(cause))
      })
  }

  /** A result of bulkDeleteUsers method
    *
    * @constructor
    *   create a new BulkDeleteUsersResult with a count of deleted Users and a
    *   list of errors
    * @param inserts
    *   a count of the effectively deleted Users
    * @param errors
    *   a list of errors catched from a user deletion
    */
  case class BulkDeleteUsersResult(inserts: Int, errors: List[String])

  // Delete several users from database
  def bulkDeleteUsers(
      users: List[User]
  )(implicit ec: ExecutionContext): Future[BulkDeleteUsersResult] = {
    Utils
      .igniteToScalaFuture(
        igniteCache.removeAllAsync(users.map(_.id).toSet.asJava)
      )
      .transformWith({
        case Success(_) =>
          Future
            .sequence(
              users.map(user =>
                Utils.igniteToScalaFuture(igniteCache.containsKeyAsync(user.id))
              )
            )
            .map(lookup => users zip lookup)
            .transformWith(lookup => {
              Future.successful(
                BulkDeleteUsersResult(
                  lookup.get.count(_._2 == false),
                  lookup.get
                    .filter(_._2 == true)
                    .map("Failed to delete user " + _._1.toString)
                )
              )
            })
        case Failure(cause) => Future.failed(UserNotPersistedException(cause))
      })
  }
}

object UserStore {
  case class GetUsersFilter(
      id: List[String] = List(),
      username: List[String] = List(),
      createdAt: Option[(String, Timestamp)] = None, // (date, (eq, lt, gt, ne))
      updatedAt: Option[(String, Timestamp)] = None // (date, (eq, lt, gt, ne))
  )
  case class GetUsersFilters(
      filters: List[GetUsersFilter] = List(),
      orderBy: List[(GetEntityFilters.Column, Int)] =
        List(), // (column, direction)
      pagination: Option[GetEntityFilters.Pagination] = None // (limit, offset)
  ) extends GetEntityFilters

  object Column {
    case class ID(val order: Int = 0, val name: String = "u.ID")
        extends GetEntityFilters.Column
    case class USERNAME(val order: Int = 1, val name: String = "u.USERNAME")
        extends GetEntityFilters.Column
    case class PASSWORD(val order: Int = 2, val name: String = "u.PASSWORD")
        extends GetEntityFilters.Column
    case class CREATED_AT(val order: Int = 3, val name: String = "u.CREATED_AT")
        extends GetEntityFilters.Column
    case class UPDATED_AT(val order: Int = 4, val name: String = "u.UPDATED_AT")
        extends GetEntityFilters.Column
    case class PROFILES(val order: Int = 5, val name: String = "PROFILES")
        extends GetEntityFilters.Column

    object PROFILE {
      case class ID(val order: Int = 0, val name: String = "p.ID")
          extends GetEntityFilters.Column
      case class LASTNAME(val order: Int = 1, val name: String = "p.LASTNAME")
          extends GetEntityFilters.Column
      case class FIRSTNAME(val order: Int = 2, val name: String = "p.FIRSTNAME")
          extends GetEntityFilters.Column
      case class LAST_LOGIN(
          val order: Int = 3,
          val name: String = "p.LAST_LOGIN"
      ) extends GetEntityFilters.Column
      case class IS_ACTIVE(val order: Int = 4, val name: String = "p.IS_ACTIVE")
          extends GetEntityFilters.Column
      case class CREATED_AT(
          val order: Int = 6,
          val name: String = "p.CREATED_AT"
      ) extends GetEntityFilters.Column
      case class UPDATED_AT(
          val order: Int = 7,
          val name: String = "p.UPDATED_AT"
      ) extends GetEntityFilters.Column
    }
  }
}
