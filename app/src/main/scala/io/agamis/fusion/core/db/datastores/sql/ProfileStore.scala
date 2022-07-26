package io.agamis.fusion.core.db.datastores.sql

import io.agamis.fusion.core.db.common.Utils
import io.agamis.fusion.core.db.datastores.sql.exceptions.NoEntryException
import io.agamis.fusion.core.db.datastores.sql.exceptions.typed.organizations.DuplicateOrganizationException
import io.agamis.fusion.core.db.datastores.sql.exceptions.typed.organizations.OrganizationNotFoundException
import io.agamis.fusion.core.db.datastores.sql.exceptions.typed.profiles.DuplicateProfileException
import io.agamis.fusion.core.db.datastores.sql.exceptions.typed.profiles.ProfileNotFoundException
import io.agamis.fusion.core.db.datastores.sql.exceptions.typed.profiles.ProfileNotPersistedException
import io.agamis.fusion.core.db.datastores.sql.exceptions.typed.profiles.ProfileQueryExecutionException
import io.agamis.fusion.core.db.datastores.sql.exceptions.typed.profiles.StillAttachedOrganizationException
import io.agamis.fusion.core.db.datastores.sql.exceptions.typed.users.UserNotFoundException
import io.agamis.fusion.core.db.datastores.sql.generics.EmailStore
import io.agamis.fusion.core.db.datastores.sql.generics.exceptions.emails.EmailNotFoundException
import io.agamis.fusion.core.db.datastores.sql.generics.exceptions.texts.TextNotFoundException
import io.agamis.fusion.core.db.datastores.typed.SqlMutableStore
import io.agamis.fusion.core.db.datastores.typed.sql.EntityFilters
import io.agamis.fusion.core.db.datastores.typed.sql.SqlStoreQuery
import io.agamis.fusion.core.db.models.sql.Profile
import io.agamis.fusion.core.db.models.sql.generics.Email
import io.agamis.fusion.core.db.models.sql.generics.Language
import io.agamis.fusion.core.db.models.sql.relations.ProfileEmail
import io.agamis.fusion.core.db.models.sql.relations.ProfileGroup
import io.agamis.fusion.core.db.models.sql.relations.ProfilePermission
import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheAtomicityMode
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.cache.QueryEntity

import java.sql.Timestamp
import java.util.UUID
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.jdk.CollectionConverters._
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import io.agamis.fusion.core.db.datastores.sql.common.Filter
import io.agamis.fusion.core.db.datastores.sql.common.exceptions.InvalidComparisonOperatorException
import io.agamis.fusion.core.db.datastores.sql.common.Placeholder
import io.agamis.fusion.core.db.datastores.sql.common.exceptions.InvalidOrderingOperatorException
import io.agamis.fusion.core.db.datastores.sql.common.Pagination
import io.agamis.fusion.core.db.datastores.sql.generics.LanguageStore
import org.apache.ignite.transactions.Transaction

class ProfileStore(implicit wrapper: IgniteClientNodeWrapper)
    extends SqlMutableStore[UUID, Profile] {

  override val schema: String = "FUSION"
  override val cache: String = s"SQL_${schema}_PROFILE"
  override protected val igniteCache: IgniteCache[UUID, Profile] =
    if (wrapper.cacheExists(cache)) {
      wrapper.getCache[UUID, Profile](cache)
    } else {
      wrapper.createCache[UUID, Profile](
        wrapper
          .makeCacheConfig[UUID, Profile]
          .setCacheMode(CacheMode.REPLICATED)
          .setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL)
          .setDataRegionName("Fusion")
          .setQueryEntities(
            List(
              new QueryEntity(classOf[String], classOf[ProfileEmail])
                .setTableName("PROFILE_EMAIL"),
              new QueryEntity(classOf[String], classOf[ProfileGroup])
                .setTableName("PROFILE_GROUP"),
              new QueryEntity(classOf[String], classOf[ProfilePermission])
                .setTableName("PROFILE_PERMISSION")
            ).asJava
          )
          .setName(cache)
          .setSqlSchema(schema)
          .setIndexedTypes(classOf[UUID], classOf[Profile])
      )
    }

  def makeProfile: Profile = {
    implicit val profileStore: ProfileStore = this
    new Profile
  }

  def makeProfilesQuery(
      queryFilters: ProfileStore.ProfilesFilters
  ): SqlStoreQuery = {
    var baseQueryString = queryString.replace("$schema", schema)
    val queryArgs: ListBuffer[String] = ListBuffer()
    val whereStatements: ListBuffer[String] = ListBuffer()
    queryFilters.filters.foreach({ filter =>
      val innerWhereStatement: ListBuffer[String] = ListBuffer()
      // manage ids search
      if (filter.id.nonEmpty) {
        innerWhereStatement += s"${ProfileStore.Column.ID().name} LIKE \"%?%\""
        queryArgs ++= filter.id
      }
      // manage aliases search
      if (filter.id.nonEmpty) {
        innerWhereStatement += s"${ProfileStore.Column.ALIAS().name} LIKE \"%?%\""
        queryArgs ++= filter.id
      }
      // manage lastnames search
      if (filter.lastname.nonEmpty) {
        innerWhereStatement += s"${ProfileStore.Column.LASTNAME().name} LIKE \"%?%\""
        queryArgs ++= filter.lastname
      }
      // manage lastnames search
      if (filter.firstname.nonEmpty) {
        innerWhereStatement += s"${ProfileStore.Column.FIRSTNAME().name} LIKE \"%?%\""
        queryArgs ++= filter.firstname
      }
      // manage lastLogin date search
      filter.lastLogin.foreach({_ match {
        case (test, time) =>
          innerWhereStatement += s"${ProfileStore.Column.LAST_LOGIN().name} ${test match {
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
      // manage shared state search
      filter.isActive match {
        case Some(value) =>
          innerWhereStatement += s"${ProfileStore.Column.IS_ACTIVE().name} = ?"
          queryArgs += (if (value) 1.toString else 0.toString)
        case None => ()
      }
      // manage metadate search
      filter.createdAt.foreach({_ match {
        case (test, time) =>
          innerWhereStatement += s"${ProfileStore.Column.CREATED_AT().name} ${test match {
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
          innerWhereStatement += s"${ProfileStore.Column.UPDATED_AT().name} ${test match {
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
      whereStatements += innerWhereStatement.mkString(" AND ")
    })
    // compile whereStatements
    baseQueryString.replace(
      Placeholder.WHERE_STATEMENT,
      whereStatements.nonEmpty match {
        case true => " WHERE " + whereStatements.reverse.mkString(" OR ")
        case false => ""
      }
    )
    // manage order
    baseQueryString.replace(
      Placeholder.ORDER_BY_STATEMENT,
      queryFilters.orderBy.nonEmpty match {
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
    // manage pagination
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

  /** Get existing profiles from database
    *
    * @param queryFilters
    * @param ec
    * @return
    */
  def getProfiles(
      queryFilters: ProfileStore.ProfilesFilters
  )(implicit ec: ExecutionContext): Future[List[Profile]] = {
    executeQuery(makeProfilesQuery(queryFilters)).transformWith({
      case Success(rows) =>
        val entityReflections = rows.groupBy(_.head)
        val profiles = rows.map({ row =>
          (for (
            profile <- Right(
              // make profile
              makeProfile
                .setId(row(ProfileStore.Column.ID().order))
                .setAlias(row(ProfileStore.Column.ALIAS().order))
                .setLastname(row(ProfileStore.Column.LASTNAME().order))
                .setFirstname(row(ProfileStore.Column.FIRSTNAME().order))
                .setLastLogin(
                  Utils.timestampFromString(row(ProfileStore.Column.LAST_LOGIN().order)) match {
                    case ts: Timestamp => ts
                    case _             => null
                  }
                )
                .setCreatedAt(
                  Utils.timestampFromString(row(ProfileStore.Column.CREATED_AT().order)) match {
                    case ts: Timestamp => ts
                    case _             => null
                  }
                )
                .setUpdatedAt(
                  Utils.timestampFromString(row(ProfileStore.Column.UPDATED_AT().order)) match {
                    case ts: Timestamp => ts
                    case _             => null
                  }
                )
            ) flatMap {
              profile =>
                Try(row(ProfileStore.Column.IS_ACTIVE().order).toBoolean) match {
                  case Success(active) =>
                    if (active) Right(profile.setActive())
                    else Right(profile.setInactive())
                  case Failure(_) => Right(profile)
                }
            } flatMap {
              profile =>
                // parse user
                for (
                  userReflection <- Right(row(ProfileStore.Column.USER().order).split("||"));
                  user <- Right(
                    new UserStore().makeUser
                      .setId(userReflection(ProfileStore.Column.USER.ID().order))
                      .setUsername(userReflection(ProfileStore.Column.USER.USERNAME().order))
                      .setPasswordHash(userReflection(ProfileStore.Column.USER.PASSWORD().order))
                      .setCreatedAt(
                        Utils.timestampFromString(userReflection(ProfileStore.Column.USER.CREATED_AT().order)) match {
                          case ts: Timestamp => ts
                          case _             => null
                        }
                      )
                      .setUpdatedAt(
                        Utils.timestampFromString(userReflection(ProfileStore.Column.USER.UPDATED_AT().order)) match {
                          case ts: Timestamp => ts
                          case _             => null
                        }
                      )
                  )
                ) yield profile.setRelatedUser(user)
            } flatMap {
              profile =>
                // parse emails
                row(ProfileStore.Column.EMAILS().order).split("||").foreach({ emailString =>
                  for (
                    emailReflection <- Right(emailString.split("|>|"));
                    email <- Right(
                      new EmailStore().makeEmail
                        .setId(emailReflection(ProfileStore.Column.EMAIL.ID().order))
                        .setAddress(emailReflection(ProfileStore.Column.EMAIL.ADDRESS().order)));
                    isMain <- Right(Try(emailReflection(ProfileStore.Column.EMAIL.IS_MAIN().order).toBoolean) match {
                          case Success(isMain) =>
                            if (isMain) true
                            else false
                          case Failure(_) => false
                        })
                  ) yield if(isMain) profile.setMainEmail(email) else profile.addEmail(email)
                })
                Right(profile)
            } flatMap {
              profile =>
                // parse organizations
                for (
                  organizationReflection <- Right(row(ProfileStore.Column.ORGANIZATION().order).split("||"));
                  organization <- Right(
                    new OrganizationStore().makeOrganization
                      .setId(organizationReflection(ProfileStore.Column.ORGANIZATION.ID().order))
                      .setLabel(organizationReflection(ProfileStore.Column.ORGANIZATION.LABEL().order))
                      .setCreatedAt(
                        Utils.timestampFromString(organizationReflection(ProfileStore.Column.ORGANIZATION.CREATED_AT().order)) match {
                          case ts: Timestamp => ts
                          case _             => null
                        }
                      )
                      .setUpdatedAt(
                        Utils.timestampFromString(organizationReflection(ProfileStore.Column.ORGANIZATION.UPDATED_AT().order)) match {
                          case ts: Timestamp => ts
                          case _             => null
                        }
                      )
                  ) flatMap {
                    org =>
                      // parse queryable field
                      Try(organizationReflection(ProfileStore.Column.ORGANIZATION.QUERYABLE().order).toBoolean) match {
                        case Success(isQueryable) =>
                          if (isQueryable) Right(org.setQueryable())
                          else Right(org.setUnqueryable())
                        case Failure(_) => Right(org)
                      }
                  } flatMap {
                    org =>
                      // parse orgType
                      for (
                        orgTypeReflection <- Right(organizationReflection(ProfileStore.Column.ORGANIZATION.ORGANIZATIONTYPE().order).split("|>|"));
                        orgType <- Right(
                          new OrganizationTypeStore().makeOrganizationType
                            .setId(orgTypeReflection(ProfileStore.Column.ORGANIZATION.ORGANIZATIONTYPE.ID().order))
                            .setLabelTextId(orgTypeReflection(ProfileStore.Column.ORGANIZATION.ORGANIZATIONTYPE.LABEL_TEXT_ID().order))
                            .setCreatedAt(
                              Utils.timestampFromString(orgTypeReflection(ProfileStore.Column.ORGANIZATION.ORGANIZATIONTYPE.CREATED_AT().order)) match {
                                case ts: Timestamp => ts
                                case _             => null
                              }
                            )
                            .setUpdatedAt(
                              Utils.timestampFromString(orgTypeReflection(ProfileStore.Column.ORGANIZATION.ORGANIZATIONTYPE.UPDATED_AT().order)) match {
                                case ts: Timestamp => ts
                                case _             => null
                              }
                            )
                        ) flatMap {
                          orgType =>
                            // parse orgType Lang variants
                            organizationReflection(ProfileStore.Column.ORGANIZATION.ORG_TYPE_LABEL().order).split("|>|")
                              .map(_.split("|>>|"))
                              .foreach({
                                orgTypeLabelReflection =>
                                  orgType.setLabel(
                                    Language.apply
                                      .setId(orgTypeLabelReflection(ProfileStore.Column.ORGANIZATION.ORG_TYPE_LABEL.LANG_ID().order))
                                      .setCode(orgTypeLabelReflection(ProfileStore.Column.ORGANIZATION.ORG_TYPE_LABEL.LANG_CODE().order))
                                      .setLabel(orgTypeLabelReflection(ProfileStore.Column.ORGANIZATION.ORG_TYPE_LABEL.LANG_LABEL().order)),
                                    orgTypeLabelReflection(ProfileStore.Column.ORGANIZATION.ORG_TYPE_LABEL.CONTENT().order)
                                  )
                              })
                            Right(orgType)
                        }
                      ) yield org.setType(orgType)
                      Right(org)
                  }
                ) yield profile.setRelatedOrganization(organization)
            } flatMap {
              profile => 
                // map groups
                row(ProfileStore.Column.GROUPS().order).split("||").map(_.split("|>|")).foreach({ group =>
                  profile.addGroup(new GroupStore().makeGroup
                    .setId(group(ProfileStore.Column.GROUP.ID().order))
                    .setName(group(ProfileStore.Column.GROUP.NAME().order))
                    .setCreatedAt(
                      Utils.timestampFromString(group(ProfileStore.Column.GROUP.CREATED_AT().order)) match {
                        case ts: Timestamp => ts
                        case _             => null
                      }
                    )
                    .setUpdatedAt(
                      Utils.timestampFromString(group(ProfileStore.Column.GROUP.UPDATED_AT().order)) match {
                        case ts: Timestamp => ts
                        case _             => null
                      }
                    )
                  )
                })
                Right(profile)
            } flatMap {
              profile =>
                // parse permissions
                row(ProfileStore.Column.PERMISSIONS().order).split("||").map(_.split("|>|")).foreach({ permissionReflection =>
                  for (
                    permission <- Right(
                      new PermissionStore().makePermission
                        .setId(permissionReflection(ProfileStore.Column.PERMISSION.ID().order))
                        .setKey(permissionReflection(ProfileStore.Column.PERMISSION.KEY().order))
                        .setCreatedAt(
                      Utils.timestampFromString(permissionReflection(ProfileStore.Column.PERMISSION.CREATED_AT().order)) match {
                          case ts: Timestamp => ts
                          case _             => null
                        }
                      )
                      .setUpdatedAt(
                        Utils.timestampFromString(permissionReflection(ProfileStore.Column.PERMISSION.UPDATED_AT().order)) match {
                          case ts: Timestamp => ts
                          case _             => null
                        }
                      )
                    ) flatMap {
                      // parse editable field
                      permission =>
                        Try(permissionReflection(ProfileStore.Column.PERMISSION.EDITABLE().order).toBoolean) match {
                          case Success(isEditable) =>
                            if (isEditable) Right(permission.setEditable)
                            else Right(permission.setReadonly)
                          case Failure(_) => Right(permission)
                        }
                    } flatMap {
                      // parse label and description variants
                      permission =>
                        permissionReflection(ProfileStore.Column.PERMISSION.LABEL().order).split("|>>|")
                          .map(_.split("|>>>|"))
                          .foreach({
                            labelReflection =>
                              permission.setLabel(
                                Language.apply
                                  .setId(labelReflection(ProfileStore.Column.PERMISSION.LABEL.LANG_ID().order))
                                  .setCode(labelReflection(ProfileStore.Column.PERMISSION.LABEL.LANG_CODE().order))
                                  .setLabel(labelReflection(ProfileStore.Column.PERMISSION.LABEL.LANG_LABEL().order)),
                                labelReflection(ProfileStore.Column.PERMISSION.LABEL.CONTENT().order)
                              )
                          })
                        permissionReflection(ProfileStore.Column.PERMISSION.DESCRIPTION().order).split("|>>|")
                          .map(_.split("|>>>|"))
                          .foreach({
                            descReflection =>
                              permission.setLabel(
                                Language.apply
                                  .setId(descReflection(ProfileStore.Column.PERMISSION.DESCRIPTION.LANG_ID().order))
                                  .setCode(descReflection(ProfileStore.Column.PERMISSION.DESCRIPTION.LANG_CODE().order))
                                  .setLabel(descReflection(ProfileStore.Column.PERMISSION.DESCRIPTION.LANG_LABEL().order)),
                                descReflection(ProfileStore.Column.PERMISSION.DESCRIPTION.CONTENT().order)
                              )
                          })
                        Right(permission)
                    }
                  ) yield profile.addPermission(permission)
                })
                Right(profile)
            }
          ) yield profile).getOrElse(null)
        })
        Future.successful(profiles.toList)
      case Failure(cause) =>
        Future.failed(ProfileQueryExecutionException(cause))
    })
  }

  def getAllProfiles(implicit ec: ExecutionContext): Future[List[Profile]] = {
    getProfiles(
      ProfileStore
        .ProfilesFilters()
        .copy(
          orderBy = List(
            (ProfileStore.Column.ID(), 1)
          )
        )
    ).transformWith({
      case Success(profiles) =>
        profiles.length match {
          case 0 => Future.failed(NoEntryException("Profile store is empty"))
          case _ => Future.successful(profiles)
        }
      case Failure(cause) => Future.failed(cause)
    })
  }

  def getProfileById(
      id: String
  )(implicit ec: ExecutionContext): Future[Profile] = {
    getProfiles(
      ProfileStore
        .ProfilesFilters()
        .copy(
          filters = List(
            ProfileStore
              .ProfilesFilter()
              .copy(
                id = Some(id)
              )
          )
        )
    ).transformWith({
      case Success(profiles) =>
        profiles.length match {
          case 0 =>
            Future.failed(
              ProfileNotFoundException(s"Profile $id couldn't be found")
            )
          case 1 => Future.successful(profiles.head)
          case _ => Future.failed(new DuplicateProfileException)
        }
      case Failure(cause) => Future.failed(cause)
    })
  }

  def persistProfile(
      profile: Profile
  )(implicit ec: ExecutionContext): Future[Transaction] = {
    (profile.relatedUser, profile.relatedOrganization) match {
      case (Some(_), Some(_)) =>
        makeTransaction match {
          case Success(tx) =>
            val emailRelationCache: IgniteCache[String, ProfileEmail] =
              wrapper.getCache[String, ProfileEmail](cache)
            val groupRelationCache: IgniteCache[String, ProfileGroup] =
              wrapper.getCache[String, ProfileGroup](cache)
            val permissionRelationCache
                : IgniteCache[String, ProfilePermission] =
              wrapper.getCache[String, ProfilePermission](cache)
            Future
              .sequence(
                List(
                  // Save entity
                  Utils.igniteToScalaFuture(
                    igniteCache.putAsync(
                      profile.id,
                      profile
                    )
                  ),
                  // Save emails
                  Utils.igniteToScalaFuture(
                    emailRelationCache.putAllAsync(
                      (profile.emails
                        .filter(_._1 == true)
                        .map({ email =>
                          (
                            s"${profile.id}:${email._2.id}",
                            ProfileEmail(
                              profile.id,
                              email._2.id
                            )
                          )
                        }) ++ List(
                        (
                          s"${profile.id}:${profile.mainEmail.id}",
                          ProfileEmail(
                            profile.id,
                            profile.mainEmail.id,
                            isMain = true
                          )
                        )
                      )).toMap[String, ProfileEmail].asJava
                    )
                  ),
                  // Remove emails
                  Utils.igniteToScalaFuture(
                    emailRelationCache.removeAllAsync(
                      profile.emails
                        .filter(_._1 == false)
                        .map({ email => s"${profile.id}:${email._2.id}" })
                        .toSet
                        .asJava
                    )
                  ),
                  // Save groups
                  Utils.igniteToScalaFuture(
                    groupRelationCache.putAllAsync(
                      profile.groups
                        .filter(_._1 == true)
                        .map({ group =>
                          (
                            s"${profile.id}:${group._2.id}",
                            ProfileGroup(
                              profile.id,
                              group._2.id
                            )
                          )
                        })
                        .toMap
                        .asJava
                    )
                  ),
                  // Remove groups
                  Utils.igniteToScalaFuture(
                    groupRelationCache.removeAllAsync(
                      profile.groups
                        .filter(_._1 == false)
                        .map({ group => s"${profile.id}:${group._2.id}" })
                        .toSet
                        .asJava
                    )
                  ),
                  // Save permissions
                  Utils.igniteToScalaFuture(
                    permissionRelationCache.putAllAsync(
                      profile.permissions
                        .filter(_._1 == true)
                        .map({ permission =>
                          (
                            s"${profile.id}:${permission._2.id}",
                            ProfilePermission(
                              profile.id,
                              permission._2.id
                            )
                          )
                        })
                        .toMap
                        .asJava
                    )
                  ),
                  // Remove permission
                  Utils.igniteToScalaFuture(
                    permissionRelationCache.removeAllAsync(
                      profile.permissions
                        .filter(_._1 == false)
                        .map({ permission =>
                          s"${profile.id}:${permission._2.id}"
                        })
                        .toSet
                        .asJava
                    )
                  )
                )
              )
              .transformWith({
                case Success(_) =>
                  Utils
                    .igniteToScalaFuture(
                      igniteCache.putAsync(
                        profile.id,
                        profile
                      )
                    )
                    .transformWith({
                      case Success(_) => Future.successful(tx)
                      case Failure(cause) =>
                        Future.failed(ProfileNotPersistedException(cause))
                    })
                case Failure(cause) =>
                  rollbackTransaction(tx)
                  Future.failed(ProfileNotPersistedException(cause))
              })
          case Failure(cause) =>
            Future.failed(ProfileNotPersistedException(cause))
        }
      case (None, _) =>
        Future.failed(
          ProfileNotPersistedException(
            "relatedUser not found and can't be set to null"
          )
        )
      case (_, None) =>
        Future.failed(
          ProfileNotPersistedException(
            "relatedOrganization not found and can't be set to null"
          )
        )
    }
  }

  /** A result of bulkPersistProfiles method
    *
    * @constructor
    *   create a new BulkPersistProfilesResult with a count of inserted Profiles
    *   and a list of errors
    * @param inserts
    *   a count of the effectively inserted Profiles
    * @param errors
    *   a list of errors catched from a profile insertion
    */
  case class BulkPersistProfilesResult(inserts: Int, errors: List[String])

  def bulkPersistProfiles(
      profiles: List[Profile]
  )(implicit ec: ExecutionContext): Future[BulkPersistProfilesResult] = {
    Utils
      .igniteToScalaFuture(
        igniteCache.putAllAsync(
          (profiles
            .filter(profile => {
              (profile.relatedUser, profile.relatedOrganization) match {
                case (Some(_), Some(_)) => true
                case (None, _)          => false
                case (_, None)          => false
              }
            })
            .map(_.id) zip profiles).toMap[UUID, Profile].asJava
        )
        // TODO: persist relations
      )
      .transformWith({
        case Success(_) =>
          Future
            .sequence(
              profiles.map(profile =>
                Utils
                  .igniteToScalaFuture(igniteCache.containsKeyAsync(profile.id))
              )
            )
            .map(lookup => profiles zip lookup)
            .transformWith(lookup => {
              Future.successful(
                BulkPersistProfilesResult(
                  lookup.get.count(_._2 == true),
                  lookup.get
                    .filter(_._2 == false)
                    .map("Insert profile " + _._1.id.toString + " failed")
                )
              )
            })
        case Failure(cause) =>
          Future.failed(ProfileNotPersistedException(cause))
      })
  }

  def deleteProfile(
      profile: Profile
  )(implicit ec: ExecutionContext): Future[Transaction] = {
    profile.relatedOrganization match {
      case Some(_) => Future.failed(StillAttachedOrganizationException())
      case None =>
        makeTransaction match {
          case Success(tx) =>
            val emailRelationCache: IgniteCache[String, ProfileEmail] =
              wrapper.getCache[String, ProfileEmail](cache)
            val groupRelationCache: IgniteCache[String, ProfileGroup] =
              wrapper.getCache[String, ProfileGroup](cache)
            val permissionRelationCache
                : IgniteCache[String, ProfilePermission] =
              wrapper.getCache[String, ProfilePermission](cache)
            val emailStore: EmailStore = new EmailStore()
            Future
              .sequence(
                List(
                  Utils.igniteToScalaFuture(
                    igniteCache.removeAsync(profile.id)
                  ),
                  // Delete relations
                  // Delete email relations
                  Utils.igniteToScalaFuture(
                    emailRelationCache.removeAllAsync(
                      profile.emails
                        .map({ email =>
                          s"${profile.id}:${email._2.id}"
                        })
                        .toSet
                        .asJava
                    )
                  ),
                  // Delete emails
                  emailStore.bulkDeleteEmails(profile.emails.map(_._2.id)),
                  // Delete group relations
                  Utils.igniteToScalaFuture(
                    groupRelationCache.removeAllAsync(
                      profile.groups
                        .map({ group =>
                          s"${profile.id}:${group._2.id}"
                        })
                        .toSet[String]
                        .asJava
                    )
                  ),
                  // Delete permission relations
                  Utils.igniteToScalaFuture(
                    permissionRelationCache.removeAllAsync(
                      profile.permissions
                        .map({ permission =>
                          s"${profile.id}:${permission._2.id}"
                        })
                        .toSet[String]
                        .asJava
                    )
                  )
                  // No need to delete organization relation as it is a foreign key in profile object
                )
              )
              .transformWith({
                case Success(_) =>
                  Future.successful(tx)
                case Failure(cause) =>
                  rollbackTransaction(tx)
                  Future.failed(ProfileNotPersistedException(cause))
              })
          case Failure(cause) =>
            Future.failed(ProfileNotPersistedException(cause))
        }
    }
  }

  /** A result of bulkDeleteProfiles method
    *
    * @constructor
    *   create a new BulkDeleteProfilesResult with a count of deleted Profiles
    *   and a list of errors
    * @param inserts
    *   a count of the actually deleted Profiles
    * @param errors
    *   a list of errors catched from profiles deletions
    */
  case class BulkDeleteProfilesResult(inserts: Int, errors: List[String])

  def bulkDeleteProfiles(
      profiles: List[Profile]
  )(implicit ec: ExecutionContext): Future[BulkDeleteProfilesResult] = {
    makeTransaction match {
      case Success(tx) =>
        val emailRelationCache: IgniteCache[String, ProfileEmail] =
          wrapper.getCache[String, ProfileEmail](cache)
        val groupRelationCache: IgniteCache[String, ProfileGroup] =
          wrapper.getCache[String, ProfileGroup](cache)
        val permissionRelationCache: IgniteCache[String, ProfilePermission] =
          wrapper.getCache[String, ProfilePermission](cache)
        val emailStore: EmailStore = new EmailStore()
        var undeletedProfilesEntries: List[(UUID, Throwable)] = null
        val profilesToDelete = profiles.filter(profile => {
          profile.relatedOrganization match {
            case Some(_) =>
              undeletedProfilesEntries ::= (
                (
                  profile.id,
                  StillAttachedOrganizationException()
                )
              ); false
            case None => true
          }
        })
        Future
          .sequence(
            List(
              Utils.igniteToScalaFuture(
                igniteCache.removeAllAsync(
                  profilesToDelete.map(_.id).toSet.asJava
                )
              ),
              // Delete relations
              // Delete all profiles email relations
              Utils.igniteToScalaFuture(
                emailRelationCache.removeAllAsync(
                  profilesToDelete
                    .flatMap(p =>
                      p.emails.map({ email =>
                        s"${p.id}:${email._2.id}"
                      })
                    )
                    .toSet
                    .asJava
                )
              ),
              // Delete all profiles emails
              emailStore.bulkDeleteEmails(
                profilesToDelete.flatMap(_.emails.map(_._2.id))
              ),
              // Delete all profiles group relations
              Utils.igniteToScalaFuture(
                groupRelationCache.removeAllAsync(
                  profilesToDelete
                    .flatMap(p =>
                      p.groups.map({ group =>
                        s"${p.id}:${group._2.id}"
                      })
                    )
                    .toSet
                    .asJava
                )
              ),
              // Delete all profiles permission relations
              Utils.igniteToScalaFuture(
                permissionRelationCache.removeAllAsync(
                  profilesToDelete
                    .flatMap(p =>
                      p.permissions.map({ permission =>
                        s"${p.id}:${permission._2.id}"
                      })
                    )
                    .toSet
                    .asJava
                )
              )
              // No need to delete all profiles organization relations as they are foreign keys in profile object
            )
          )
          .transformWith({
            case Success(_) =>
              commitTransaction(tx).transformWith({
                case Success(_) =>
                  Future
                    .sequence(
                      profiles.map(profile =>
                        Utils.igniteToScalaFuture(
                          igniteCache.containsKeyAsync(profile.id)
                        )
                      )
                    )
                    .map(lookup => profiles zip lookup)
                    .transformWith(lookup => {
                      Future.successful(
                        BulkDeleteProfilesResult(
                          lookup.get.count(_._2 == true),
                          lookup.get
                            .filter(_._2 == false)
                            .map(lookup =>
                              undeletedProfilesEntries.toMap
                                .get(lookup._1.id) match {
                                case Some(cause) =>
                                  "Failed to delete profile " + lookup._1.id.toString + ": " + cause.getMessage
                                case None =>
                                  "Failed to delete profile " + lookup._1.id.toString
                              }
                            )
                        )
                      )
                    })
                case Failure(cause) =>
                  Future.failed(ProfileNotPersistedException(cause))
              })
            case Failure(cause) =>
              rollbackTransaction(tx)
              Future.failed(ProfileNotPersistedException(cause))
          })
      case Failure(cause) => Future.failed(ProfileNotPersistedException(cause))
    }
  }
}

object ProfileStore {
  case class ProfilesFilter(
      id: Option[String] = None,
      alias: Option[String] = None,
      lastname: Option[String] = None,
      firstname: Option[String] = None,
      lastLogin: List[(String, Timestamp)] = List(), // (date, (eq, lt, gt, ne))
      isActive: Option[Boolean] = None,
      createdAt: Option[(String, Timestamp)] = None, // (date, (eq, lt, gt, ne))
      updatedAt: Option[(String, Timestamp)] = None // (date, (eq, lt, gt, ne))
  )

  case class ProfilesFilters(
      filters: List[ProfilesFilter] = List(),
      orderBy: List[(EntityFilters.Column, Int)] =
        List(), // (column, direction)
      pagination: Option[EntityFilters.Pagination] = None // (limit, offset)
  ) extends EntityFilters

  object Column {
    case class ID(val order: Int = 0, val name: String = "p.ID")
        extends EntityFilters.Column
    case class ALIAS(val order: Int = 1, val name: String = "p.ALIAS")
        extends EntityFilters.Column
    case class LASTNAME(val order: Int = 2, val name: String = "p.LASTNAME")
        extends EntityFilters.Column
    case class FIRSTNAME(val order: Int = 3, val name: String = "p.FIRSTNAME")
        extends EntityFilters.Column
    case class LAST_LOGIN(val order: Int = 4, val name: String = "p.LAST_LOGIN")
        extends EntityFilters.Column
    case class IS_ACTIVE(val order: Int = 5, val name: String = "p.IS_ACTIVE")
        extends EntityFilters.Column
    case class CREATED_AT(val order: Int = 6, val name: String = "p.CREATED_AT")
        extends EntityFilters.Column
    case class UPDATED_AT(val order: Int = 7, val name: String = "p.UPDATED_AT")
        extends EntityFilters.Column
    case class USER(val order: Int = 8, val name: String = "USER")
        extends EntityFilters.Column
    case class EMAILS(val order: Int = 9, val name: String = "EMAILS")
        extends EntityFilters.Column
    case class ORGANIZATION(val order: Int = 10, val name: String = "ORGANIZATION")
        extends EntityFilters.Column
    case class GROUPS(val order: Int = 11, val name: String = "GROUPS")
        extends EntityFilters.Column
    case class PERMISSIONS(val order: Int = 12, val name: String = "PERMISSIONS")
        extends EntityFilters.Column
    object USER {
      case class ID(val order: Int = 0, val name: String = "u.ID")
          extends EntityFilters.Column
      case class USERNAME(val order: Int = 1, val name: String = "u.USERNAME")
          extends EntityFilters.Column
      case class PASSWORD(val order: Int = 2, val name: String = "u.PASSWORD")
          extends EntityFilters.Column
      case class CREATED_AT(val order: Int = 3, val name: String = "u.CREATED_AT")
          extends EntityFilters.Column
      case class UPDATED_AT(val order: Int = 4, val name: String = "u.UPDATED_AT")
          extends EntityFilters.Column
    }

    object EMAIL {
      case class ID(val order: Int = 0, val name: String = "e.ID")
          extends EntityFilters.Column
      case class ADDRESS(val order: Int = 1, val name: String = "e.ADDRESS")
          extends EntityFilters.Column
      case class IS_MAIN(val order: Int = 2, val name: String = "pe.IS_MAIN")
          extends EntityFilters.Column
    }

    object ORGANIZATION {
      case class ID(val order: Int = 0, val name: String = "o.ID")
          extends EntityFilters.Column
      case class LABEL(val order: Int = 1, val name: String = "o.LABEL")
          extends EntityFilters.Column
      case class QUERYABLE(val order: Int = 2, val name: String = "o.QUERYABLE")
          extends EntityFilters.Column
      case class CREATED_AT(val order: Int = 3, val name: String = "o.CREATED_AT")
          extends EntityFilters.Column
      case class UPDATED_AT(val order: Int = 4, val name: String = "o.UPDATED_AT")
          extends EntityFilters.Column
      case class ORGANIZATIONTYPE(val order: Int = 5, val name: String = "o.ORGANIZATIONTYPE")
          extends EntityFilters.Column
      case class ORG_TYPE_LABEL(val order: Int = 6, val name: String = "o.ORG_TYPE_LABEL")
          extends EntityFilters.Column

      object ORGANIZATIONTYPE {
        case class ID(val order: Int = 0, val name: String = "ot.ID")
            extends EntityFilters.Column
        case class LABEL_TEXT_ID(val order: Int = 1, val name: String = "ot.LABEL_TEXT_ID")
            extends EntityFilters.Column
        case class CREATED_AT(val order: Int = 2, val name: String = "ot.CREATED_AT")
            extends EntityFilters.Column
        case class UPDATED_AT(val order: Int = 3, val name: String = "ot.UPDATED_AT")
            extends EntityFilters.Column
      }

      object ORG_TYPE_LABEL {
        case class TEXT_ID(val order: Int = 0, val name: String = "t.ID")
            extends EntityFilters.Column
        case class LANG_ID(val order: Int = 1, val name: String = "l.ID")
            extends EntityFilters.Column
        case class LANG_CODE(val order: Int = 2, val name: String = "l.CODE")
            extends EntityFilters.Column
        case class LANG_LABEL(val order: Int = 3, val name: String = "l.LABEL")
            extends EntityFilters.Column
        case class CONTENT(val order: Int = 4, val name: String = "t.CONTENT")
            extends EntityFilters.Column
      }
    }

    object GROUP {
      case class ID(val order: Int = 0, val name: String = "g.ID")
          extends EntityFilters.Column
      case class NAME(val order: Int = 1, val name: String = "g.NAME")
          extends EntityFilters.Column
      case class CREATED_AT(val order: Int = 2, val name: String = "g.CREATED_AT")
          extends EntityFilters.Column
      case class UPDATED_AT(val order: Int = 3, val name: String = "g.UPDATED_AT")
          extends EntityFilters.Column
    }

    object PERMISSION {
      case class ID(val order: Int = 0, val name: String = "perm.ID")
          extends EntityFilters.Column
      case class KEY(val order: Int = 1, val name: String = "perm.`KEY`")
          extends EntityFilters.Column
      case class EDITABLE(val order: Int = 2, val name: String = "perm.EDITABLE")
          extends EntityFilters.Column
      case class APPLICATION_ID(val order: Int = 3, val name: String = "perm.APPLICATION_ID")
          extends EntityFilters.Column
      case class CREATED_AT(val order: Int = 4, val name: String = "perm.CREATED_AT")
          extends EntityFilters.Column
      case class UPDATED_AT(val order: Int = 5, val name: String = "perm.UPDATED_AT")
          extends EntityFilters.Column
      case class LABEL(val order: Int = 6, val name: String = "perm.LABEL")
          extends EntityFilters.Column
      case class DESCRIPTION(val order: Int = 7, val name: String = "perm.DESCRIPTION")
          extends EntityFilters.Column

      object LABEL {
        case class TEXT_ID(val order: Int = 0, val name: String = "t.ID")
            extends EntityFilters.Column
        case class LANG_ID(val order: Int = 1, val name: String = "l.ID")
            extends EntityFilters.Column
        case class LANG_CODE(val order: Int = 2, val name: String = "l.CODE")
            extends EntityFilters.Column
        case class LANG_LABEL(val order: Int = 3, val name: String = "l.LABEL")
            extends EntityFilters.Column
        case class CONTENT(val order: Int = 4, val name: String = "t.CONTENT")
            extends EntityFilters.Column
      }

      object DESCRIPTION {
        case class TEXT_ID(val order: Int = 0, val name: String = "td.ID")
            extends EntityFilters.Column
        case class LANG_ID(val order: Int = 1, val name: String = "ld.ID")
            extends EntityFilters.Column
        case class LANG_CODE(val order: Int = 2, val name: String = "ld.CODE")
            extends EntityFilters.Column
        case class LANG_LABEL(val order: Int = 3, val name: String = "ld.LABEL")
            extends EntityFilters.Column
        case class CONTENT(val order: Int = 4, val name: String = "td.CONTENT")
            extends EntityFilters.Column
      }
    }
  }
}
