package io.agamis.fusion.core.db.datastores.sql

import io.agamis.fusion.core.db.common.Utils
import io.agamis.fusion.core.db.datastores.sql.exceptions.NoEntryException
import io.agamis.fusion.core.db.datastores.sql.exceptions.typed.permissions.{DuplicatePermissionException, PermissionNotFoundException, PermissionNotPersistedException, PermissionQueryExecutionException}
import io.agamis.fusion.core.db.datastores.sql.generics.exceptions.texts.TextNotFoundException
import io.agamis.fusion.core.db.datastores.typed.SqlMutableStore
import io.agamis.fusion.core.db.datastores.typed.sql.{GetEntityFilters, SqlStoreQuery}
import io.agamis.fusion.core.db.models.sql.{Application, Permission}
import io.agamis.fusion.core.db.models.sql.exceptions.applications.InvalidApplicationStatus
import io.agamis.fusion.core.db.models.sql.generics.{Email, Language}
import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.{CacheAtomicityMode, CacheMode}

import java.sql.Timestamp
import java.util.UUID
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

class PermissionStore(implicit wrapper: IgniteClientNodeWrapper) extends SqlMutableStore[UUID, Permission] {

  override val schema: String = "FUSION"
  override val cache: String = s"SQL_${schema}_PERMISSION"
  override protected val igniteCache: IgniteCache[UUID, Permission] = if (wrapper.cacheExists(cache)) {
    wrapper.getCache[UUID, Permission](cache)
  } else {
    wrapper.createCache[UUID, Permission](
      wrapper.makeCacheConfig[UUID, Permission]
        .setCacheMode(CacheMode.REPLICATED)
        .setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL)
        .setDataRegionName("Fusion")
        .setName(cache)
        .setSqlSchema(schema)
        .setIndexedTypes(classOf[UUID], classOf[Permission])
    )
  }

  // Create Permission object
  def makePermission: Permission = {
    implicit val permissionStore: PermissionStore = this
    new Permission
  }

  def makePermissionQuery(queryFilters: PermissionStore.GetPermissionsFilters): SqlStoreQuery = {
    var baseQueryString = queryString.replace("$schema", schema)
    val queryArgs: ListBuffer[String] = ListBuffer()
    val whereStatements: ListBuffer[String] = ListBuffer()
    queryFilters.filters.foreach({ filter =>
      val innerWhereStatement: ListBuffer[String] = ListBuffer()
      // manage ids search
      if (filter.id.nonEmpty) {
        innerWhereStatement += s"permission_id in (${(for (_ <- 1 to filter.id.length) yield "?").mkString(",")})"
        queryArgs ++= filter.id
      }
      // manage keys search
      if (filter.key.nonEmpty) {
        innerWhereStatement += s"permission_key in (${(for (_ <- 1 to filter.key.length) yield "?").mkString(",")})"
        queryArgs ++= filter.key
      }
      // manage editable state search
      filter.editable match {
        case Some(value) =>
          innerWhereStatement += s"permission_editable = ?"
          queryArgs += value.toString
        case None => ()
      }
      // manage metadate search
      filter.createdAt match {
        case Some((test, time)) =>
          innerWhereStatement += s"permission_created_at ${
            test match {
              case "eq" => "="
              case "gt" => ">"
              case "lt" => "<"
              case "neq" => "<>"
            }
          } ?"
          queryArgs += time.toString
        case None => ()
      }
      filter.updatedAt match {
        case Some((test, time)) =>
          innerWhereStatement += s"permission_updated_at ${
            test match {
              case "eq" => "="
              case "gt" => ">"
              case "lt" => "<"
              case "neq" => "<>"
            }
          } ?"
          queryArgs += time.toString
        case None => ()
      }
      whereStatements += innerWhereStatement.mkString(" AND ")
    })
    // compile whereStatements
    if (whereStatements.nonEmpty) {
      baseQueryString += " WHERE " + whereStatements.reverse.mkString(" OR ")
    }
    // manage order
    if (queryFilters.orderBy.nonEmpty) {
      baseQueryString += s" ORDER BY ${
        queryFilters.orderBy.map(o =>
          s"permission_${o._1} ${
            o._2 match {
              case 1 => "ASC"
              case -1 => "DESC"
            }
          }"
        ).mkString(", ")
      }"
    }
    makeQuery(baseQueryString)
      .setParams(queryArgs.toList)
  }

  def getPermissions(queryFilters: PermissionStore.GetPermissionsFilters)(implicit ec: ExecutionContext): Future[List[Permission]] = {
    executeQuery(
      makePermissionQuery(queryFilters)
    ).transformWith({
      case Success(rows) =>
        val entityReflections = rows.groupBy(_.head)
        val permissions = rows.map(_.head).distinct.map(entityReflections(_)).map(entityReflection => {
          //permission_id, permission_key, permission_editable, permission_label_text_id, permission_description_text_id, permission_created_at, permission_updated_at, info_data, type_data
          val groupedRows = getRelationsGroupedRowsFrom(entityReflection, 7, 8)
          groupedRows.get("PERMISSION") match {
            case Some(permissionReflections) =>
              val permissionDef = permissionReflections.head.head._2
              (for (
                permission <- Right(makePermission
                  .setId(permissionDef(0))
                  .setKey(permissionDef(1))
                  .setLabelTextId(permissionDef(3))
                  .setDescriptionTextId(permissionDef(4))
                  .setCreatedAt(Utils.timestampFromString(permissionDef(5)) match {
                    case createdAt: Timestamp => createdAt
                    case _ => null
                  })
                  .setUpdatedAt(Utils.timestampFromString(permissionDef(6)) match {
                    case updatedAt: Timestamp => updatedAt
                    case _ => null
                  })
                ) flatMap { permission =>
                  Try(permissionDef(2).toBoolean) match {
                    case Success(editable) =>
                      if (editable) Right(permission.setEditable)
                      else Right(permission.setReadonly)
                    case Failure(_) => Right(permission)
                  }
                } flatMap { permission =>
                  permissionReflections.head.partition(_._1 == "PERMISSION_LABEL_LANG_VARIANT") match {
                    case result =>
                      if (result._1.isEmpty) {
                        Future.failed(TextNotFoundException(s"Permission ${permission.id} might lack of label in any language"))
                      } else {
                        result._1.foreach({ labelLangVariant =>
                          val variantDef = labelLangVariant._2
                          permission.setLabel(
                            Language.apply
                              .setId(variantDef(3))
                              .setCode(variantDef(2))
                              .setLabel(variantDef(4)),
                            variantDef(1)
                          )
                        })
                      }
                      result._2.filter(_._1 == "PERMISSION_DESC_LANG_VARIANT") match {
                        case result =>
                          if (result.isEmpty) {
                            Future.failed(TextNotFoundException(s"Permission ${permission.id} might lack of description in any language"))
                          } else {
                            result.foreach({ descLangVariant =>
                              val variantDef = descLangVariant._2
                              permission.setDescription(
                                Language.apply
                                  .setId(variantDef(3))
                                  .setCode(variantDef(2))
                                  .setLabel(variantDef(4)),
                                variantDef(1)
                              )
                            })
                          }
                      }
                  }
                  Right(permission)
                } flatMap { permission =>
                  groupedRows.get("APPLICATION") match {
                    case Some(applicationReflections) =>
                      applicationReflections.foreach({ applicationReflection =>
                        val applicationDef = applicationReflection(0)._2
                        for (
                          application <- Right(new ApplicationStore()
                            .makeApplication
                            .setId(applicationDef(0))
                            .setLabel(applicationDef(1))
                            .setVersion(applicationDef(2))
                            .setAppUniversalId(applicationDef(3))
                            .setManifestUrl(applicationDef(5))
                            .setStoreUrl(applicationDef(6))
                            .setCreatedAt(Utils.timestampFromString(applicationDef(7)) match {
                              case createdAt: Timestamp => createdAt
                              case _ => null
                            })
                            .setUpdatedAt(Utils.timestampFromString(applicationDef(8)) match {
                              case updatedAt: Timestamp => updatedAt
                              case _ => null
                            })
                          ) flatMap { application =>
                            try {
                              application.setStatus(Application.Status.fromInt(applicationDef(4).asInstanceOf[Int]).get)
                            } catch {
                              case e: InvalidApplicationStatus => Future.failed(new PermissionQueryExecutionException(e))
                              case e: Throwable => Future.failed(e)
                            }
                            Right(application)
                          }
                        ) yield permission.setRelatedApplication(application)
                      })
                    case None =>
                  }

                  groupedRows.get("PROFILE") match {
                    case Some(profileReflections) =>
                      profileReflections.foreach({ profileReflection =>
                        val profileDef = profileReflection(0)._2
                        for (
                          profile <- Right(new ProfileStore()
                            .makeProfile
                            .setId(profileDef(0))
                            .setLastname(profileDef(1))
                            .setFirstname(profileDef(2))
                            .setLastLogin(Utils.timestampFromString(profileDef(4)) match {
                              case lastLogin: Timestamp => lastLogin
                              case _ => null
                            })
                            .setCreatedAt(Utils.timestampFromString(profileDef(6)) match {
                              case createdAt: Timestamp => createdAt
                              case _ => null
                            })
                            .setUpdatedAt(Utils.timestampFromString(profileDef(7)) match {
                              case updatedAt: Timestamp => updatedAt
                              case _ => null
                            })
                          ) flatMap { profile =>
                            Try(profileDef(5).toBoolean) match {
                              case Success(isActive) =>
                                if (isActive) Right(profile.setActive())
                                else Right(profile.setInactive())
                              case Failure(_) => Right(profile)
                            }
                          } flatMap { profile =>
                            val profileMainEmailDef = profileDef(3).split(";")
                            profileMainEmailDef.length match {
                              case 2 =>
                                Right(profile.setMainEmail(Email.apply
                                  .setId(profileMainEmailDef(0))
                                  .setAddress(profileMainEmailDef(1))
                                ))
                              case _ => Right(profile)
                            }
                          }
                        ) yield permission.addOwningProfile(profile)
                      })
                    case None =>
                  }

                  groupedRows.get("GROUP") match {
                    case Some(groupReflections) =>
                      groupReflections.foreach({ groupReflection =>
                        val groupDef = groupReflection(0)._2
                        for (
                          group <- Right(new GroupStore()
                            .makeGroup
                            .setId(groupDef(0))
                            .setName(groupDef(1))
                            .setCreatedAt(Utils.timestampFromString(groupDef(2)) match {
                              case createdAt: Timestamp => createdAt
                              case _ => null
                            })
                            .setUpdatedAt(Utils.timestampFromString(groupDef(3)) match {
                              case updatedAt: Timestamp => updatedAt
                              case _ => null
                            })
                          )
                        ) yield permission.addOwningGroup(group)
                      })
                    case None =>
                  }
                  Right(permission)
                }
              ) yield permission).getOrElse(null)
            case None =>
          }
        })
        Future.successful(permissions.toList.asInstanceOf[List[Permission]])
      case Failure(cause) => Future.failed(PermissionQueryExecutionException(cause))
    })
  }

  def getAllPermissions(implicit ec: ExecutionContext): Future[List[Permission]] = {
    getPermissions(PermissionStore.GetPermissionsFilters().copy(
      orderBy = List(
        ("id", 1)
      )
    )).transformWith({
      case Success(permissions) =>
        if (permissions.isEmpty) {
          Future.failed(NoEntryException("Permission store is empty"))
        } else {
          Future.successful(permissions)
        }
      case Failure(cause) => Future.failed(cause)
    })
  }

  def getPermissionById(id: String)(implicit ec: ExecutionContext): Future[Permission] = {
    getPermissions(
      PermissionStore.GetPermissionsFilters().copy(
        filters = List(
          PermissionStore.GetPermissionsFilter().copy(
            id = List(id)
          )
        )
      )
    ).transformWith({
      case Success(permissions) =>
        permissions.length match {
          case 0 => Future.failed(PermissionNotFoundException(s"Permission $id couldn't be found"))
          case 1 => Future.successful(permissions.head)
          case _ => Future.failed(new DuplicatePermissionException)
        }
      case Failure(cause) => Future.failed(cause)
    })
  }

  def getPermissionByKey(key: String)(implicit ec: ExecutionContext): Future[Permission] = {
    getPermissions(
      PermissionStore.GetPermissionsFilters().copy(
        filters = List(
          PermissionStore.GetPermissionsFilter().copy(
            key = List(key)
          )
        )
      )
    ).transformWith({
      case Success(permissions) =>
        permissions.length match {
          case 0 => Future.failed(PermissionNotFoundException(s"Permission $key couldn't be found"))
          case 1 => Future.successful(permissions.head)
          case _ => Future.failed(new DuplicatePermissionException)
        }
      case Failure(cause) => Future.failed(cause)
    })
  }

  def persistPermission(permission: Permission)(implicit ec: ExecutionContext): Future[Unit] = {
    makeTransaction match {
      case Success(tx) =>
        val profileStore = new ProfileStore()
        val groupStore = new GroupStore()
        Future.sequence(
          List(
            Utils.igniteToScalaFuture(igniteCache.putAsync(permission.id, permission))
          ) ++ {
            if (permission.owningProfiles.nonEmpty) {
              List(
                profileStore.getProfiles(
                  ProfileStore.GetProfilesFilters()
                    .copy(
                      filters = List(
                        ProfileStore.GetProfilesFilter()
                          .copy(
                            id = permission.owningProfiles.map(_._2.id.toString)
                          )
                      ),
                      orderBy = List(
                        ("id", 1)
                      )
                    )
                ).transformWith({
                  case Success(profiles) =>
                    profileStore.bulkPersistProfiles(
                      (permission.owningProfiles.sortBy(_._2.id).map(_._1) zip profiles).map({ profile =>
                        if (profile._1) {
                          profile._2.addPermission(permission)
                        } else {
                          profile._2.removePermission(permission)
                        }
                      })
                    )
                  case Failure(cause) => Future.failed(cause)
                })
              )
            } else Nil
          } ++ {
            if (permission.owningGroups.nonEmpty) {
              List(
                groupStore.getGroups(
                  GroupStore.GetGroupsFilters()
                    .copy(
                      filters = List(
                        GroupStore.GetGroupsFilter()
                          .copy(
                            id = permission.owningGroups.map(_._2.id.toString)
                          )
                      ),
                      orderBy = List(
                        ("id", 1)
                      )
                    )
                ).transformWith({
                  case Success(groups) =>
                    groupStore.bulkPersistGroups(
                      (permission.owningGroups.sortBy(_._2.id).map(_._1) zip groups).map({ group =>
                        if (group._1) {
                          group._2.addPermission(permission)
                        } else {
                          group._2.removePermission(permission)
                        }
                      })
                    )
                  case Failure(cause) => Future.failed(cause)
                })
              )
            } else Nil
          }
        ).transformWith({
          case Success(_) =>
            commitTransaction(tx).transformWith({
              case Success(_) => Future.unit
              case Failure(cause) => Future.failed(PermissionNotPersistedException(cause))
            })
          case Failure(cause) =>
            rollbackTransaction(tx)
            Future.failed(PermissionNotPersistedException(cause))
        })
      case Failure(cause) => Future.failed(PermissionNotPersistedException(cause))
    }
  }

  def bulkPersistPermissions(permissions: List[Permission])(implicit ec: ExecutionContext): Future[Unit] = {
    val transaction = makeTransaction
    transaction match {
      case Success(_) =>
        val profileStore = new ProfileStore()
        val groupStore = new GroupStore()
        Future.sequence(
          List(
            Utils.igniteToScalaFuture(igniteCache.putAllAsync(
              (permissions.map(_.id) zip permissions).toMap[UUID, Permission].asJava
            )),
            profileStore.getProfiles(
              ProfileStore.GetProfilesFilters()
                .copy(
                  filters = List(
                    ProfileStore.GetProfilesFilter()
                      .copy(
                        id = permissions
                          .flatMap({ permission =>
                            permission.owningProfiles.map(_._2)
                          })
                          .distinctBy(_.id)
                          .map(_.id.toString)
                      )
                  )
                )
            ).transformWith({
              case Success(profiles) =>
                val profilesPermissions =
                  permissions flatMap { permission =>
                    permission.owningProfiles map { profile =>
                      (profile._2.id, (profile._1, permission))
                    }
                  } groupBy (_._1)
                profileStore.bulkPersistProfiles(
                  profiles.map({ profile =>
                    profilesPermissions.get(profile.id) match {
                      case Some(relations) =>
                        relations.foreach({ r =>
                          if (r._2._1) {
                            profile.addPermission(r._2._2)
                          } else {
                            profile.removePermission(r._2._2)
                          }
                        })
                      case None =>
                    }
                    profile
                  })
                )
              case Failure(cause) => Future.failed(cause)
            }),
            groupStore.getGroups(
              GroupStore.GetGroupsFilters()
                .copy(
                  filters = List(
                    GroupStore.GetGroupsFilter()
                      .copy(
                        id = permissions
                          .flatMap({ permission =>
                            permission.owningGroups.map(_._2)
                          })
                          .distinctBy(_.id)
                          .map(_.id.toString)
                      )
                  )
                )
            ).transformWith({
              case Success(groups) =>
                val groupsPermissions =
                  permissions flatMap { permission =>
                    permission.owningGroups map { group =>
                      (group._2.id, (group._1, permission))
                    }
                  } groupBy (_._1)
                groupStore.bulkPersistGroups(
                  groups.map({ group =>
                    groupsPermissions.get(group.id) match {
                      case Some(relations) =>
                        relations.foreach({ r =>
                          if (r._2._1) {
                            group.addPermission(r._2._2)
                          } else {
                            group.removePermission(r._2._2)
                          }
                        })
                      case None =>
                    }
                    group
                  })
                )
              case Failure(cause) => Future.failed(cause)
            })
          )
        ).transformWith({
          case Success(_) => Future.unit
          case Failure(cause) => Future.failed(PermissionNotPersistedException(cause))
        })
      case Failure(cause) => Future.failed(PermissionNotPersistedException(cause))
    }
  }

  def deletePermission(permission: Permission)(implicit ec: ExecutionContext): Future[Unit] = {
    makeTransaction match {
      case Success(tx) =>
        val profileStore = new ProfileStore()
        val groupStore = new GroupStore()
        Future.sequence(
          List(
            // Handle permissions removal
            Utils.igniteToScalaFuture(igniteCache.removeAsync(permission.id))
          ) ++ {
            // Handle permission removal in profiles
            if (permission.owningProfiles.nonEmpty) {
              List(
                profileStore.getProfiles(
                  ProfileStore.GetProfilesFilters()
                    .copy(
                      filters = List(
                        ProfileStore.GetProfilesFilter()
                          .copy(
                            id = permission.owningProfiles.map(_._2.id.toString)
                          )
                      ),
                      orderBy = List(
                        ("id", 1)
                      )
                    )
                ).transformWith({
                  case Success(profiles) =>
                    profileStore.bulkPersistProfiles(
                      (permission.owningProfiles.sortBy(_._2.id).map(_._1) zip profiles).map({ profile =>
                        profile._2.removePermission(permission)
                      })
                    )
                  case Failure(cause) => Future.failed(cause)
                })
              )
            } else Nil
          } ++ {
            // Handle permission removal in groups
            if (permission.owningGroups.nonEmpty) {
              List(
                groupStore.getGroups(
                  GroupStore.GetGroupsFilters()
                    .copy(
                      filters = List(
                        GroupStore.GetGroupsFilter()
                          .copy(
                            id = permission.owningGroups.map(_._2.id.toString)
                          )
                      ),
                      orderBy = List(
                        ("id", 1)
                      )
                    )
                ).transformWith({
                  case Success(groups) =>
                    groupStore.bulkPersistGroups(
                      (permission.owningGroups.sortBy(_._2.id).map(_._1) zip groups).map({ group =>
                        group._2.removePermission(permission)
                      })
                    )
                  case Failure(cause) => Future.failed(cause)
                })
              )
            } else Nil
          }
        ).transformWith({
          case Success(_) =>
            commitTransaction(tx).transformWith({
              case Success(_) => Future.unit
              case Failure(cause) => Future.failed(cause)
            })
          case Failure(cause) =>
            rollbackTransaction(tx)
            Future.failed(PermissionNotPersistedException(cause))
        })
      case Failure(cause) =>
        Future.failed(PermissionNotPersistedException(cause))
    }
  }
}

object PermissionStore {
  case class GetPermissionsFilter(
                                   id: List[String] = List(),
                                   key: List[String] = List(),
                                   editable: Option[Boolean] = None,
                                   createdAt: Option[(String, Timestamp)] = None,
                                   updatedAt: Option[(String, Timestamp)] = None
                                 )

  case class GetPermissionsFilters(
                                    filters: List[GetPermissionsFilter] = List(),
                                    orderBy: List[(String, Int)] = List()
                                  ) extends GetEntityFilters
}