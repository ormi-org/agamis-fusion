package io.ogdt.fusion.core.db.datastores.sql

import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import io.ogdt.fusion.core.db.datastores.typed.sql.SqlStoreQuery
import io.ogdt.fusion.core.db.datastores.typed.SqlMutableStore
import io.ogdt.fusion.core.db.datastores.typed.sql.GetEntityFilters
import java.sql.Timestamp
import org.apache.ignite.IgniteCache
import java.util.UUID
import scala.jdk.CollectionConverters._
import io.ogdt.fusion.core.db.models.sql.Permission
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.cache.CacheAtomicityMode
import scala.concurrent.ExecutionContext
import io.ogdt.fusion.core.db.datastores.sql.exceptions.permissions.PermissionNotPersistedException
import io.ogdt.fusion.core.db.common.Utils
import scala.concurrent.Future
import scala.util.Success
import scala.util.Failure
import scala.collection.mutable.ListBuffer
import io.ogdt.fusion.core.db.datastores.sql.exceptions.permissions.PermissionQueryExecutionException
import java.time.Instant
import io.ogdt.fusion.core.db.models.sql.Application
import io.ogdt.fusion.core.db.models.sql.exceptions.applications.InvalidApplicationStatus
import scala.util.Try
import io.ogdt.fusion.core.db.models.sql.generics.Email

class PermissionStore(implicit wrapper: IgniteClientNodeWrapper) extends SqlMutableStore[UUID, Permission] {

    override val schema: String = "FUSION"
    override val cache: String = s"SQL_${schema}_PERMISSION"
    override protected var igniteCache: IgniteCache[UUID, Permission] = wrapper.cacheExists(cache) match {
        case true => wrapper.getCache[UUID, Permission](cache)
        case false => {
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
    }

    // Create Permission object 
    def makePermission: Permission = {
        implicit val permissionStore: PermissionStore = this
        new Permission
    }

    def makePermissionQuery(queryFilters: PermissionStore.GetPermissionsFilters): SqlStoreQuery = {
        var queryString: String = 
            "SELECT permission_id, permission_key, permission_editable, permission_label_text_id, permission_description_text_id, permission_created_at, permission_updated_at, info_data, type_data " +
            "FROM " +
            "(SELECT PERMISSION.id AS permission_id, PERMISSION.key AS permission_key, PERMISSION.editable AS permission_editable, PERMISSION.label_text_id AS permission_label_text_id, PERMISSION.description_text_id AS permission_description_text_id, PERMISSION.created_at AS permission_created_at, PERMISSION.updated_at AS permission_updated_at, " +
            "CONCAT_WS('||', PROFILE.id, lastname, firstname, CONCAT_WS(';', EMAIL.id, EMAIL.address), last_login, is_active, PROFILE.created_at , PROFILE.updated_at) AS info_data, 'PROFILE' AS type_data " +
            "FROM FUSION.PERMISSION AS PERMISSION " +
            "RIGHT OUTER JOIN FUSION.PROFILE_PERMISSION AS PROF_PER ON PROF_PER.permission_id = PERMISSION.id " +
            "LEFT OUTER JOIN FUSION.PROFILE AS PROFILE ON PROFILE.id = PROF_PER.profile_id " +
            "LEFT OUTER JOIN FUSION.PROFILE_EMAIL AS PROFILE_EMAIL ON PROFILE_EMAIL.profile_id = PROFILE.id " +
            "LEFT OUTER JOIN FUSION.EMAIL AS EMAIL ON PROFILE_EMAIL.email_id = EMAIL.id " +
            "WHERE PROFILE_EMAIL.is_main = TRUE " +
            "UNION ALL " +
            "SELECT PERMISSION.id AS permission_id, PERMISSION.key AS permission_key, PERMISSION.editable AS permission_editable, PERMISSION.label_text_id AS permission_label_text_id, PERMISSION.description_text_id AS permission_description_text_id, PERMISSION.created_at AS permission_created_at, PERMISSION.updated_at AS permission_updated_at, " +
            "CONCAT_WS('||', \"GROUP\".id, \"GROUP\".name, \"GROUP\".created_at, \"GROUP\".updated_at) AS info_data, 'GROUP' AS type_data " +
            "FROM FUSION.PERMISSION AS PERMISSION " +
            "RIGHT OUTER JOIN FUSION.GROUP_PERMISSION AS GROUP_PER ON GROUP_PER.permission_id = PERMISSION.id " +
            "LEFT OUTER JOIN FUSION.\"GROUP\" AS \"GROUP\" ON \"GROUP\".id = GROUP_PER.group_id " +
            "UNION ALL " +
            "SELECT PERMISSION.id AS permission_id, PERMISSION.key AS permission_key, PERMISSION.editable AS permission_editable, PERMISSION.label_text_id AS permission_label_text_id, PERMISSION.description_text_id AS permission_description_text_id, PERMISSION.created_at AS permission_created_at, PERMISSION.updated_at AS permission_updated_at, " +
            "CONCAT_WS('||', APP.id, APP.label, APP.version, APP.app_universal_id, APP.status, APP.manifest_url, APP.store_url, APP.created_at, APP.updated_at) AS info_data, 'APPLICATION' AS type_data " +
            "FROM FUSION.PERMISSION AS PERMISSION " +
            "INNER JOIN FUSION.APPLICATION AS APP ON APP.id = PERMISSION.application_id	" +
            "UNION ALL " +
            "(SELECT permission_id, permission_key, permission_editable, permission_label_text_id, permission_description_text_id, permission_created_at, permission_updated_at, " +
            "CONCAT_WS('||', permission_id, GROUP_CONCAT(content SEPARATOR ';'), code, language_id, label, GROUP_CONCAT(text_id SEPARATOR ';')) AS info_data, 'PERMISSION_LANG_VARIANT' AS type_data " +
            "FROM " +
            "(SELECT PERMISSION.id AS permission_id, TEXT.content, LANG.code, TEXT.language_id, LANG.label, TEXT.id AS text_id, PERMISSION.key AS permission_key, PERMISSION.editable AS permission_editable, PERMISSION.label_text_id AS permission_label_text_id, PERMISSION.description_text_id AS permission_description_text_id, PERMISSION.created_at AS permission_created_at, PERMISSION.updated_at AS permission_updated_at " +
            "FROM FUSION.PERMISSION AS PERMISSION " +
            "LEFT OUTER JOIN FUSION.TEXT AS TEXT ON TEXT.id = PERMISSION.label_text_id " +
            "LEFT OUTER JOIN FUSION.LANGUAGE AS LANG ON TEXT.language_id = LANG.id " +
            "UNION ALL " +
            "SELECT PERMISSION.id AS permission_id, TEXT.content, LANG.code, TEXT.language_id, LANG.label, TEXT.id AS text_id, PERMISSION.key AS permission_key, PERMISSION.editable AS permission_editable, PERMISSION.label_text_id AS permission_label_text_id, PERMISSION.description_text_id AS permission_description_text_id, PERMISSION.created_at AS permission_created_at, PERMISSION.updated_at AS permission_updated_at " +
            "FROM FUSION.PERMISSION AS PERMISSION " +
            "LEFT OUTER JOIN FUSION.TEXT AS TEXT ON TEXT.id = PERMISSION.description_text_id " +
            "LEFT OUTER JOIN FUSION.LANGUAGE AS LANG ON TEXT.language_id = LANG.id) " +
            "GROUP BY permission_id, language_id))"
        var queryArgs: ListBuffer[String] = ListBuffer()
        var whereStatements: ListBuffer[String] = ListBuffer()
        queryFilters.filters.foreach({ filter =>
            var innerWhereStatement: ListBuffer[String] = ListBuffer()
            // manage ids search
            if (!filter.id.isEmpty) {
                innerWhereStatement += s"permission_id in (${(for (i <- 1 to filter.id.length) yield "?").mkString(",")})"
                queryArgs ++= filter.id
            }
            // manage keys search
            if (!filter.key.isEmpty) {
                innerWhereStatement += s"permission_key in (${(for (i <- 1 to filter.key.length) yield "?").mkString(",")})"
                queryArgs ++= filter.key
            }
            // manage editable state search
            filter.editable match {
                case Some(value) => {
                    innerWhereStatement += s"permission_editable = ?"
                    queryArgs += value.toString
                }
                case None => ()
            }
            // manage metadate search
            filter.createdAt match {
                case Some((test, time)) => {
                    innerWhereStatement += s"permission_created_at ${
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
                    innerWhereStatement += s"permission_updated_at ${
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
        if (!whereStatements.isEmpty) {
            queryString += " WHERE " + whereStatements.reverse.mkString(" OR ")
        }
        // manage order
        if (!queryFilters.orderBy.isEmpty) {
            queryString += s" ORDER BY ${queryFilters.orderBy.map( o =>
                s"permission_${o._1} ${o._2 match {
                    case 1 => "ASC"
                    case -1 => "DESC"
                }}"
            ).mkString(", ")}"
        }
        makeQuery(queryString)
        .setParams(queryArgs.toList)
    }

    def getPermissions(queryFilters: PermissionStore.GetPermissionsFilters)(implicit ec: ExecutionContext): Future[List[Permission]] = {
        executeQuery(makePermissionQuery(queryFilters)).transformWith({
            case Success(permissionResults) => {
                val permissions = permissionResults.toList.groupBy(_(0)).map(entityReflection => {
                    (for (
                        permission <- Right(makePermission
                            .setId(entityReflection._2(0)(0).toString)
                            .setKey(entityReflection._2(0)(1).toString)
                            .setLabelTextId(entityReflection._2(0)(3).toString)
                            .setDescriptionTextId(entityReflection._2(0)(4).toString)
                            .setCreatedAt(entityReflection._2(0)(5) match {
                                case createdAt: Timestamp => createdAt
                                case _ => null
                            })
                            .setUpdatedAt(entityReflection._2(0)(6) match {
                                case updatedAt: Timestamp => updatedAt
                                case _ => null
                            })
                        ) flatMap { permission =>
                            entityReflection._2(0)(2) match {
                                case editable: Boolean => {
                                    if (editable) Right(permission.setEditable)
                                    else Right(permission.setReadonly)
                                }
                                case _ => Right(permission)
                            }
                        } flatMap { permission =>

                            val groupedRows = getRelationsGroupedRowsFrom(entityReflection._2, 7, 8)

                            groupedRows.get("APPLICATION") match {
                                case Some(applicationReflections) => {
                                    applicationReflections.foreach({ applicationReflection =>
                                        val applicationDef = applicationReflection(0)._2
                                        (for (
                                            application <- Right(new ApplicationStore()
                                                .makeApplication
                                                .setId(applicationDef(0))
                                                .setLabel(applicationDef(1))
                                                .setVersion(applicationDef(2))
                                                .setAppUniversalId(applicationDef(3))
                                                .setManifestUrl(applicationDef(5))
                                                .setStoreUrl(applicationDef(6))
                                                .setCreatedAt(Timestamp.from(Instant.parse(applicationDef(7))) match {
                                                    case createdAt: Timestamp => createdAt
                                                    case _ => null
                                                })
                                                .setUpdatedAt(Timestamp.from(Instant.parse(applicationDef(8))) match {
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
                                        ) yield permission.setRelatedApplication(application))
                                    })
                                }
                                case None => {}
                            }

                            groupedRows.get("PROFILE") match {
                                case Some(profileReflections) => {
                                    profileReflections.foreach({ profileReflection =>
                                        val profileDef = profileReflection(0)._2
                                        (for (
                                            profile <- Right(new ProfileStore()
                                                .makeProfile
                                                .setId(profileDef(0))
                                                .setLastname(profileDef(1))
                                                .setFirstname(profileDef(2))
                                                .setLastLogin(Timestamp.from(Instant.parse(profileDef(4))) match {
                                                    case lastLogin: Timestamp => lastLogin
                                                    case _ => null
                                                })
                                                .setCreatedAt(Timestamp.from(Instant.parse(profileDef(6))) match {
                                                    case createdAt: Timestamp => createdAt
                                                    case _ => null
                                                })
                                                .setUpdatedAt(Timestamp.from(Instant.parse(profileDef(7))) match {
                                                    case updatedAt: Timestamp => updatedAt
                                                    case _ => null
                                                })
                                            ) flatMap { profile =>
                                                Try(profileDef(5).toBoolean) match {
                                                    case Success(isActive) => {
                                                        if (isActive) Right(profile.setActive)
                                                        else Right(profile.setInactive)
                                                    }
                                                    case Failure(cause) => Right(profile)
                                                }
                                            } flatMap { profile =>
                                                val profileMainEmailDef = profileDef(3).split(";")
                                                profileMainEmailDef.length match {
                                                    case 2 => {
                                                        Right(profile.setMainEmail(Email.apply
                                                            .setId(profileMainEmailDef(0))
                                                            .setAddress(profileMainEmailDef(1))
                                                        ))
                                                    }
                                                    case _ => Right(profile)
                                                }
                                            }
                                        ) yield permission.addOwningProfile(profile))
                                    })
                                }
                                case None => {}
                            }

                            groupedRows.get("GROUP") match {
                                case Some(groupReflections) =>  {
                                    groupReflections.foreach({ groupReflection =>
                                        val groupDef = groupReflection(0)._2
                                        (for (
                                            group <- Right(new GroupStore()
                                                .makeGroup
                                                .setId(groupDef(0))
                                                .setName(groupDef(1))
                                                .setCreatedAt(Timestamp.from(Instant.parse(groupDef(2))) match {
                                                    case createdAt: Timestamp => createdAt
                                                    case _ => null
                                                })
                                                .setUpdatedAt(Timestamp.from(Instant.parse(groupDef(3))) match {
                                                    case updatedAt: Timestamp => updatedAt
                                                    case _ => null
                                                })
                                            )
                                        ) yield permission.addOwningGroup(group))
                                    })
                                }
                                case None => {}
                            }
                            Right(permission)
                        }
                    ) yield permission).getOrElse(null)
                })
                Future.successful(permissions.toList)
            }
            case Failure(cause) => Future.failed(PermissionQueryExecutionException(cause))
        })
    }

    def persistPermission(permission: Permission)(implicit ec: ExecutionContext): Future[Unit] = {
        val transaction = makeTransaction
        transaction match {
            case Success(tx) => {
                val profileStore = new ProfileStore()
                val groupStore = new GroupStore()
                Future.sequence(
                    List(
                        Utils.igniteToScalaFuture(igniteCache.putAsync(permission.id, permission))
                    ) ++ {
                        if (!permission.owningProfiles.isEmpty) {
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
                                    case Success(profiles) => {
                                        profileStore.bulkPersistProfiles(
                                            (permission.owningProfiles.sortBy(_._2.id).map(_._1) zip profiles).map({ profile =>
                                                if (profile._1) {
                                                    profile._2.addPermission(permission)
                                                } else {
                                                    profile._2.removePermission(permission)
                                                }
                                            })
                                        )
                                    }
                                    case Failure(cause) => Future.failed(cause)
                                })
                            )
                        } else Nil
                    } ++ {
                        if (!permission.owningGroups.isEmpty) {
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
                                    case Success(groups) => {
                                        groupStore.bulkPersistGroups(
                                            (permission.owningGroups.sortBy(_._2.id).map(_._1) zip groups).map({ group =>
                                                if (group._1) {
                                                    group._2.addPermission(permission)
                                                } else {
                                                    group._2.removePermission(permission)
                                                }
                                            })
                                        )
                                    }
                                    case Failure(cause) => Future.failed(cause)
                                })
                            )
                        } else Nil
                    }
                ).transformWith({
                    case Success(value) => {
                        commitTransaction(transaction).transformWith({
                            case Success(value) => Future.unit
                            case Failure(cause) => Future.failed(PermissionNotPersistedException(cause))
                        })
                    }
                    case Failure(cause) => {
                        rollbackTransaction(transaction)
                        Future.failed(PermissionNotPersistedException(cause))
                    }
                })
            }
            case Failure(cause) => Future.failed(PermissionNotPersistedException(cause))
        }
    }

    def bulkPersistPermissions(permissions: List[Permission])(implicit ec: ExecutionContext): Future[Unit] = {
        val transaction = makeTransaction
        transaction match {
            case Success(tx) => {
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
                            case Success(profiles) => {
                                val profilesPermissions = 
                                    permissions flatMap { permission =>
                                        permission.owningProfiles map { profile =>
                                            (profile._2.id, (profile._1, permission))
                                        }
                                    } groupBy(_._1)
                                profileStore.bulkPersistProfiles(
                                    profiles.map({ profile => 
                                        profilesPermissions.get(profile.id) match {
                                            case Some(relations) => {
                                                relations.foreach({ r =>
                                                    if (r._2._1) {
                                                        profile.addPermission(r._2._2)
                                                    } else {
                                                        profile.removePermission(r._2._2)
                                                    }
                                                })
                                            }
                                            case None => {}
                                        }
                                        profile
                                    })
                                )
                            }
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
                            case Success(groups) => {
                                val groupsPermissions = 
                                    permissions flatMap { permission =>
                                        permission.owningGroups map { group =>
                                            (group._2.id, (group._1, permission))
                                        }
                                    } groupBy(_._1)
                                groupStore.bulkPersistGroups(
                                    groups.map({ group =>
                                        groupsPermissions.get(group.id) match {
                                            case Some(relations) => {
                                                relations.foreach({ r =>
                                                    if (r._2._1) {
                                                        group.addPermission(r._2._2)
                                                    } else {
                                                        group.removePermission(r._2._2)
                                                    }
                                                })
                                            }
                                            case None => {}
                                        }
                                        group
                                    })
                                )
                            }
                            case Failure(cause) => Future.failed(cause)
                        })
                    )
                ).transformWith({
                    case Success(value) => Future.unit
                    case Failure(cause) => Future.failed(PermissionNotPersistedException(cause))
                })
            }
            case Failure(cause) => Future.failed(PermissionNotPersistedException(cause))
        }
    }

    def deletePermission(permission: Permission)(implicit ec: ExecutionContext): Future[Unit] = {
        val transaction = makeTransaction
        transaction match {
            case Success(tx) => {
                val profileStore = new ProfileStore()
                val groupStore = new GroupStore()
                Future.sequence(
                    List(
                        // Handle permissions removal
                        Utils.igniteToScalaFuture(igniteCache.removeAsync(permission.id))
                    ) ++ {
                        // Handle permission removal in profiles
                        if (!permission.owningProfiles.isEmpty) {
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
                                    case Success(profiles) => {
                                        profileStore.bulkPersistProfiles(
                                            (permission.owningProfiles.sortBy(_._2.id).map(_._1) zip profiles).map({ profile =>
                                                profile._2.removePermission(permission)
                                            })
                                        )
                                    }
                                    case Failure(cause) => Future.failed(cause)
                                })
                            )
                        } else Nil
                    } ++ {
                        // Handle permission removal in groups
                        if (!permission.owningGroups.isEmpty) {
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
                                    case Success(groups) => {
                                        groupStore.bulkPersistGroups(
                                            (permission.owningGroups.sortBy(_._2.id).map(_._1) zip groups).map({ group =>
                                                group._2.removePermission(permission)
                                            })
                                        )
                                    }
                                    case Failure(cause) => Future.failed(cause)
                                })
                            )
                        } else Nil
                    }
                ).transformWith({
                    case Success(value) => Future.unit
                    case Failure(cause) => Future.failed(PermissionNotPersistedException(cause))
                })
            }
            case Failure(cause) => Future.failed(PermissionNotPersistedException(cause))
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
        orderBy: List[(String,Int)] = List()
    ) extends GetEntityFilters
}