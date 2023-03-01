package io.agamis.fusion.core.db.datastores.sql

import io.agamis.fusion.core.db.common.Utils
import io.agamis.fusion.core.db.datastores.sql.common.Filter
import io.agamis.fusion.core.db.datastores.sql.common.Pagination
import io.agamis.fusion.core.db.datastores.sql.common.Placeholder
import io.agamis.fusion.core.db.datastores.sql.common.exceptions.InvalidComparisonOperatorException
import io.agamis.fusion.core.db.datastores.sql.common.exceptions.InvalidOrderingOperatorException
import io.agamis.fusion.core.db.datastores.sql.exceptions.NoEntryException
import io.agamis.fusion.core.db.datastores.sql.exceptions.typed.users.DuplicateUserException
import io.agamis.fusion.core.db.datastores.sql.exceptions.typed.users.UserNotFoundException
import io.agamis.fusion.core.db.datastores.sql.exceptions.typed.users.UserNotPersistedException
import io.agamis.fusion.core.db.datastores.sql.exceptions.typed.users.UserQueryExecutionException
import io.agamis.fusion.core.db.datastores.typed.SqlMutableStore
import io.agamis.fusion.core.db.datastores.typed.sql.EntityQueryParams
import io.agamis.fusion.core.db.datastores.typed.sql.SqlStoreQuery
import io.agamis.fusion.core.db.models.sql.User
import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheAtomicityMode
import org.apache.ignite.cache.CacheMode

import java.sql.Timestamp
import java.util.UUID
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.jdk.CollectionConverters._
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import org.apache.ignite.transactions.Transaction

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

  def transmutate(u: User): User = {
    u.setStore(this)
  }

  def makeUsersQuery(queryParams: UserStore.UserQueryParams): SqlStoreQuery = {
    var baseQueryString = queryString.replace(Placeholder.SCHEMA, schema)
    val queryArgs: ListBuffer[String] = ListBuffer()
    val whereStatements: ListBuffer[String] = ListBuffer()
    queryParams.filters.foreach({ filter =>
      val innerWhereStatement: ListBuffer[String] = ListBuffer()
      // manage ids search
      if (filter.id.nonEmpty) {
        innerWhereStatement += filter.id.map({ id =>
          queryArgs += id
          s"${UserStore.Column.ID().name} LIKE \"%?%\""
        }).mkString(" OR ")
      }
      // manage usernames search
      if (filter.username.nonEmpty) {
        innerWhereStatement += filter.username.map({ f =>
          queryArgs += f._2
          s"${UserStore.Column.USERNAME().name} ${f._1 match {
            case Filter.Type.Equals => "= ?"
            case Filter.Type.Like   => "LIKE \"%?%\""
          }}"
        }).mkString(" OR ")
      }
      // manage relations filtering
      if (filter.profile_id.nonEmpty) {
        innerWhereStatement += filter.profile_id.map({ profile_id =>
          queryArgs += profile_id
          s"${EntityQueryParams.IN_WHERE_CLAUSE(UserStore.Column.PROFILE.ID().name)} LIKE \"%?%\""
        }).mkString(" OR ")
      }
      if (filter.profile_alias.nonEmpty) {
        innerWhereStatement += filter.profile_alias.map({ profile_alias =>
          queryArgs += profile_alias
          s"${EntityQueryParams.IN_WHERE_CLAUSE(UserStore.Column.PROFILE.ALIAS().name)} LIKE \"%?%\""
        }).mkString(" OR ")
      }
      if (filter.profile_lastname.nonEmpty) {
        innerWhereStatement += filter.profile_lastname.map({ profile_lastname =>
          queryArgs += profile_lastname
          s"${EntityQueryParams.IN_WHERE_CLAUSE(UserStore.Column.PROFILE.LASTNAME().name)} LIKE \"%?%\""
        }).mkString(" OR ")
      }
      if (filter.profile_firstname.nonEmpty) {
        innerWhereStatement += filter.profile_firstname.map({ profile_firstname =>
          queryArgs += profile_firstname
          s"${EntityQueryParams.IN_WHERE_CLAUSE(UserStore.Column.PROFILE.FIRSTNAME().name)} LIKE \"%?%\""
        }).mkString(" OR ")
      }
      filter.profile_lastLogin.map({ _ match {
        case (test, time) =>
          innerWhereStatement += s"${EntityQueryParams.IN_WHERE_CLAUSE(UserStore.Column.PROFILE.LAST_LOGIN().name)} ${test match {
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
      }})
      filter.profile_createdAt.foreach({_ match {
        case (test, time) =>
          innerWhereStatement += s"${EntityQueryParams.IN_WHERE_CLAUSE(UserStore.Column.PROFILE.CREATED_AT().name)} ${test match {
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
      }})
      filter.profile_updatedAt.foreach({_ match {
        case (test, time) =>
          innerWhereStatement += s"${EntityQueryParams.IN_WHERE_CLAUSE(UserStore.Column.PROFILE.UPDATED_AT().name)} ${test match {
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
      }})
      // manage metadate search
      filter.createdAt.foreach({_ match {
        case (test, time) =>
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
      }})
      filter.updatedAt.foreach({_ match {
        case (test, time) =>
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
      }})
      if (innerWhereStatement.nonEmpty) whereStatements += innerWhereStatement.mkString(" AND ")
    })
    // compile whereStatements
    baseQueryString = baseQueryString.replace(
      Placeholder.WHERE_STATEMENT,
      whereStatements.nonEmpty match {
        case true  => " WHERE " + whereStatements.reverse.mkString(" OR ")
        case false => ""
      }
    )
    // manage order
    baseQueryString = baseQueryString.replace(
      Placeholder.ORDER_BY_STATEMENT,
      queryParams.orderBy.nonEmpty match {
        case true =>
          s" ORDER BY ${queryParams.orderBy
            .map(o =>
              s"u.${o._1.name} ${o._2 match {
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
    // manage pagination
    baseQueryString = baseQueryString.replace(
      Placeholder.PAGINATION,
      queryParams.pagination match {
        case Some(p) => s" LIMIT ${p.limit} OFFSET ${p.offset} "
        case None    => s" LIMIT ${Pagination.Default.Limit} OFFSET ${Pagination.Default.Offset} "
      }
    )
    makeQuery(baseQueryString)
      .setParams(queryArgs.toList)
  }

  // Get existing users from database
  def getUsers(queryParams: UserStore.UserQueryParams)(implicit ec: ExecutionContext): Future[List[User]] = {
    executeQuery(makeUsersQuery(queryParams)).transformWith({
      case Success(rows) =>
        val users = rows.map({ row =>
          (for (
            user <- Right(
              // make user
              makeUser
                .setId(row(UserStore.Column.ID().order).asInstanceOf[UUID])
                .setUsername(row(UserStore.Column.USERNAME().order).asInstanceOf[String])
                .setPasswordHash(row(UserStore.Column.PASSWORD().order).asInstanceOf[String])
                .setCreatedAt(
                  row(UserStore.Column.CREATED_AT().order).asInstanceOf[Timestamp] match {
                    case ts: Timestamp => ts
                    case _             => null
                  }
                )
                .setUpdatedAt(
                  row(UserStore.Column.UPDATED_AT().order).asInstanceOf[Timestamp] match {
                    case ts: Timestamp => ts
                    case _             => null
                  }
                )
            ) flatMap {
              user => 
                // map profiles
                row(UserStore.Column.PROFILES().order).asInstanceOf[String].split("""\|\|""").foreach({ profileString =>
                  for (
                    profileReflection <- Right(profileString.split("""\|>\|"""));
                    profile <- Right(
                      new ProfileStore().makeProfile
                        .setId(profileReflection(UserStore.Column.PROFILE.ID().order))
                        .setLastname(profileReflection(UserStore.Column.PROFILE.LASTNAME().order))
                        .setFirstname(profileReflection(UserStore.Column.PROFILE.FIRSTNAME().order))
                        .setLastLogin(
                          Utils.timestampFromString(profileReflection(UserStore.Column.PROFILE.LAST_LOGIN().order)) match {
                            case ts: Timestamp => ts
                            case _             => null
                          }
                        )
                        .setRelatedUser(user)
                        .setCreatedAt(
                          Utils.timestampFromString(profileReflection(UserStore.Column.PROFILE.CREATED_AT().order)) match {
                            case ts: Timestamp => ts
                            case _             => null
                          }
                        )
                        .setUpdatedAt(
                          Utils.timestampFromString(profileReflection(UserStore.Column.PROFILE.UPDATED_AT().order)) match {
                            case ts: Timestamp => ts
                            case _             => null
                          }
                        )
                    ) flatMap { profile =>
                      Try(profileReflection(UserStore.Column.PROFILE.IS_ACTIVE().order).toBoolean) match {
                        case Success(isActive) =>
                          if (isActive) Right(profile.setActive())
                          else Right(profile.setInactive())
                        case Failure(_) => Right(profile)
                      }
                    }
                  ) yield user.addRelatedProfile(profile)
                })
                Right(user)
            }
          ) yield user).getOrElse(null)
        })
        Future.successful(users.toList)
      case Failure(cause) => Future.failed(UserQueryExecutionException(cause))
    })
  }

  def getAllUsers(implicit ec: ExecutionContext): Future[List[User]] = {
    getUsers(
      UserStore
        .UserQueryParams()
        .copy(
          orderBy = List(
            (UserStore.Column.ID(), 1)
          )
        )
    ).transformWith({
      case Success(users) =>
        users.length match {
          case 0 => Future.failed(NoEntryException("User table is empty"))
          case _ => Future.successful(users.map(transmutate(_)))
        }
      case Failure(cause) => Future.failed(cause)
    })
  }

  /** Method used to get a single User entity from database based on its id attribute
    * 
    * @param id the id selector
    * @param ec the '''implicit''' [[ExecutionContext ExecutionContext]] used to parallelize computing
    * @return a future [[User User]] which reflects user state fetched from database
    */
  def getUserById(id: String)(implicit ec: ExecutionContext): Future[User] = {
    Utils.igniteToScalaFuture(igniteCache.getAsync(UUID.fromString(id)))
    .transformWith({
      case Success(user) =>
        user match {
          case _: User => Future.successful(transmutate(user))
          case null    => Future.failed(UserNotFoundException(s"User $id couldn't be found"))
        }
      case Failure(cause) => Future.failed(cause)
    })
  }

  def getUserByUsername(
      username: String
  )(implicit ec: ExecutionContext): Future[User] = {
    getUsers(
      UserStore
        .UserQueryParams()
        .copy(
          filters = List(
            UserStore
              .UsersFilter()
              .copy(
                username = List((Filter.Type.Equals, username))
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
          case 1 => Future.successful(transmutate(users.head))
          case _ => Future.failed(new DuplicateUserException)
        }
      case Failure(cause) => Future.failed(cause)
    })
  }

  // Save user object's modification to database
  def persistUser(user: User)(implicit ec: ExecutionContext): Future[(Transaction, User)] = {
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
            case Success(_) => Future.successful((tx, user))
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
  def deleteUser(user: User)(implicit ec: ExecutionContext): Future[(Transaction, User)] = {
    makeTransaction match {
      case Success(tx) =>
        Utils.igniteToScalaFuture(igniteCache.removeAsync(user.id))
        .transformWith({
          case Success(_) =>
            Future.successful(tx, user)
          case Failure(cause) =>
            tx.rollbackAsync()
            Future.failed(UserNotPersistedException(cause))
        })
      case Failure(cause) => Future.failed(cause)
    }
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
  case class UsersFilter(
      id: List[String] = List(),
      username: List[(Filter.Type.Value, String)] = List(),
      profile_id: List[String] = List(),
      profile_alias: List[String] = List(),
      profile_lastname: List[String] = List(),
      profile_firstname: List[String] = List(),
      profile_lastLogin: List[(String, Timestamp)] = List(), // (date, (eq, lt, gt, ne))
      profile_createdAt: List[(String, Timestamp)] = List(), // (date, (eq, lt, gt, ne))
      profile_updatedAt: List[(String, Timestamp)] = List(), // (date, (eq, lt, gt, ne))
      createdAt: List[(String, Timestamp)] = List(), // (date, (eq, lt, gt, ne))
      updatedAt: List[(String, Timestamp)] = List() // (date, (eq, lt, gt, ne))
  )
  case class UserQueryParams(
      filters: List[UsersFilter] = List(),
      orderBy: List[(EntityQueryParams.Column, Int)] =
        List(), // (column, direction)
      pagination: Option[EntityQueryParams.Pagination] = None // (limit, offset)
  ) extends EntityQueryParams

  object Column {
    case class ID(val order: Int = 0, val name: String = "u.ID")
        extends EntityQueryParams.Column
    case class USERNAME(val order: Int = 1, val name: String = "u.USERNAME")
        extends EntityQueryParams.Column
    case class PASSWORD(val order: Int = 2, val name: String = "u.PASSWORD")
        extends EntityQueryParams.Column
    case class CREATED_AT(val order: Int = 3, val name: String = "u.CREATED_AT")
        extends EntityQueryParams.Column
    case class UPDATED_AT(val order: Int = 4, val name: String = "u.UPDATED_AT")
        extends EntityQueryParams.Column
    case class PROFILES(val order: Int = 5, val name: String = "PROFILES")
        extends EntityQueryParams.Column

    object PROFILE {
      case class ID(val order: Int = 0, val name: String = "p.ID")
          extends EntityQueryParams.Column
      case class ALIAS(val order: Int = 1, val name: String = "p.ALIAS")
          extends EntityQueryParams.Column
      case class LASTNAME(val order: Int = 2, val name: String = "p.LASTNAME")
          extends EntityQueryParams.Column
      case class FIRSTNAME(val order: Int = 3, val name: String = "p.FIRSTNAME")
          extends EntityQueryParams.Column
      case class LAST_LOGIN(val order: Int = 4, val name: String = "p.LAST_LOGIN")
          extends EntityQueryParams.Column
      case class IS_ACTIVE(val order: Int = 5, val name: String = "p.IS_ACTIVE")
          extends EntityQueryParams.Column
      case class CREATED_AT(val order: Int = 6, val name: String = "p.CREATED_AT")
          extends EntityQueryParams.Column
      case class UPDATED_AT(val order: Int = 7, val name: String = "p.UPDATED_AT")
          extends EntityQueryParams.Column
    }
  }
}
