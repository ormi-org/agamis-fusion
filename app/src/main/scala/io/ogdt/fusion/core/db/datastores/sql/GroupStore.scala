package io.ogdt.fusion.core.db.datastores.sql

import io.ogdt.fusion.core.db.datastores.sql.OrganizationStore
import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import io.ogdt.fusion.core.db.datastores.typed.SqlMutableStore
import io.ogdt.fusion.core.db.datastores.typed.sql.GetEntityFilters

import org.apache.ignite.IgniteCache
import scala.concurrent.Future
import scala.util.Success
import scala.util.Failure

import java.sql.Timestamp
import java.util.UUID
import io.ogdt.fusion.core.db.common.Utils

import scala.jdk.CollectionConverters._
import org.apache.ignite.cache.CacheMode
import io.ogdt.fusion.core.db.datastores.typed.sql.SqlStoreQuery
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext
import org.apache.ignite.cache.CacheAtomicityMode

import io.ogdt.fusion.core.db.datastores.sql.exceptions.groups.{
    GroupNotPersistedException,
    GroupQueryExecutionException,
    DuplicateGroupException,
    GroupNotFoundException
}

import org.apache.ignite.cache.QueryEntity
import scala.util.Try
import io.ogdt.fusion.core.db.datastores.sql.generics.EmailStore
import io.ogdt.fusion.core.db.models.sql.OrganizationType
import io.ogdt.fusion.core.db.models.sql.generics.Language
import io.ogdt.fusion.core.db.models.sql.Group
import io.ogdt.fusion.core.db.models.sql.relations.GroupPermission
import io.ogdt.fusion.core.db.datastores.sql.exceptions.organizations.OrganizationNotFoundException
import io.ogdt.fusion.core.db.models.sql.generics.Email
import java.time.Instant
import io.ogdt.fusion.core.db.datastores.sql.exceptions.organizations.DuplicateOrganizationException
import io.ogdt.fusion.core.db.datastores.sql.exceptions.permissions.PermissionNotFoundException
import io.ogdt.fusion.core.db.datastores.sql.exceptions.NoEntryException

class GroupStore(implicit wrapper: IgniteClientNodeWrapper) extends SqlMutableStore[UUID, Group] {

    override val schema: String = "FUSION"
    override val cache: String = s"SQL_${schema}_GROUP"
    override protected var igniteCache: IgniteCache[UUID, Group] = wrapper.cacheExists(cache) match {
        case true => wrapper.getCache[UUID, Group](cache)
        case false => {
            wrapper.createCache[UUID, Group](
                wrapper.makeCacheConfig[UUID, Group]
                .setCacheMode(CacheMode.REPLICATED)
                .setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL)
                .setDataRegionName("Fusion")
                .setQueryEntities(
                    List(
                        new QueryEntity(classOf[String], classOf[GroupPermission])
                        .setTableName("GROUP_PERMISSION")
                    ).asJava
                )
                .setName(cache)
                .setSqlSchema(schema)
                .setIndexedTypes(classOf[UUID], classOf[Group])
            )
        }
    }

    def makeGroup: Group = {
        implicit val profileStore: GroupStore = this
        new Group
    }

    def makeGroupsQuery(queryFilters: GroupStore.GetGroupsFilters): SqlStoreQuery = {
        var queryString: String =
            "SELECT group_id, group_name, group_created_at, group_updated_at, info_data, type_data " +
            "FROM( " +
            "SELECT \"GROUP\".id AS group_id, \"GROUP\".name AS group_name, \"GROUP\".created_at AS group_created_at, \"GROUP\".updated_at AS group_updated_at, " +
            "CONCAT_WS('||', PROFILE.id, lastname, firstname, CONCAT_WS(';', EMAIL.id, EMAIL.address), last_login, is_active, user_id, PROFILE.organization_id , PROFILE.created_at , PROFILE.updated_at) AS info_data, 'PROFILE' AS type_data  " +
            "FROM FUSION.\"GROUP\" AS \"GROUP\" " +
            "LEFT OUTER JOIN FUSION.PROFILE_GROUP AS PROFILE_GROUP ON PROFILE_GROUP.group_id = \"GROUP\".id " +
            "LEFT OUTER JOIN FUSION.PROFILE AS PROFILE ON PROFILE_GROUP.profile_id = PROFILE.id " +
            "LEFT OUTER JOIN FUSION.PROFILE_EMAIL AS PROFILE_EMAIL ON PROFILE_EMAIL.profile_id = PROFILE.id " +
            "LEFT OUTER JOIN FUSION.EMAIL AS EMAIL ON PROFILE_EMAIL.email_id = EMAIL.id " +
            "WHERE PROFILE_EMAIL.is_main = TRUE " +
            "UNION ALL " +
            "SELECT \"GROUP\".id AS group_id, \"GROUP\".name AS group_name, \"GROUP\".created_at AS group_created_at, \"GROUP\".updated_at AS group_updated_at, " +
            "CONCAT_WS('||', ORG.id, ORG.label, ORG.queryable, ORG.created_at, ORG.updated_at) AS info_data, 'ORGANIZATION' AS type_data " +
            "FROM FUSION.\"GROUP\" AS \"GROUP\" " +
            "INNER JOIN FUSION.ORGANIZATION AS ORG ON \"GROUP\".organization_id = ORG.id " +
            "UNION ALL( " +
            "SELECT group_id, group_name, group_created_at, group_updated_at, info_data, type_data " +
            "FROM ( " +
            "SELECT \"GROUP\".id AS group_id, \"GROUP\".name AS group_name, \"GROUP\".created_at AS group_created_at, \"GROUP\".updated_at AS group_updated_at, " +
            "CONCAT_WS('||', ORG.id, ORGTYPE.id, ORGTYPE.label_text_id, ORGTYPE.created_at, ORGTYPE.updated_at) AS info_data, 'ORGTYPE' AS type_data, ORGTYPE.id AS orgtype_id " +
            "FROM FUSION.\"GROUP\" AS \"GROUP\" " +
            "LEFT OUTER JOIN FUSION.ORGANIZATION AS ORG ON \"GROUP\".organization_id = ORG.id " +
            "LEFT OUTER JOIN FUSION.ORGANIZATIONTYPE AS ORGTYPE ON ORG.organizationtype_id = ORGTYPE.id " +
            "UNION ALL( " +
            "SELECT \"GROUP\".id AS group_id, \"GROUP\".name AS group_name, \"GROUP\".created_at AS group_created_at, \"GROUP\".updated_at AS group_updated_at, " +
            "CONCAT_WS('||', ORG.id, ORGTYPE.id, TEXT.content, LANG.code, TEXT.language_id, LANG.label) AS info_data, 'ORGTYPE_LANG_VARIANT' AS type_data, ORGTYPE.id AS orgtype_id " +
            "FROM FUSION.\"GROUP\" AS \"GROUP\" " +
            "LEFT OUTER JOIN FUSION.ORGANIZATION AS ORG ON \"GROUP\".organization_id = ORG.id " +
            "LEFT OUTER JOIN FUSION.ORGANIZATIONTYPE AS ORGTYPE ON ORG.organizationtype_id = ORGTYPE.id " +
            "LEFT OUTER JOIN FUSION.TEXT AS TEXT ON TEXT.id = ORGTYPE.label_text_id " +
            "LEFT OUTER JOIN FUSION.LANGUAGE AS LANG ON TEXT.language_id = LANG.id " +
            ")) " +
            "ORDER BY orgtype_id " +
            ") " +
            "UNION ALL( " +
            "SELECT group_id, group_name, group_created_at, group_updated_at, info_data, type_data " +
            "FROM( " +
            "SELECT \"GROUP\".id AS group_id, \"GROUP\".name AS group_name, \"GROUP\".created_at AS group_created_at, \"GROUP\".updated_at AS group_updated_at, " +
            "CONCAT_WS('||', PERMISSION.id, PERMISSION.key, PERMISSION.label_text_id, PERMISSION.description_text_id, PERMISSION.editable, PERMISSION.application_id, PERMISSION.created_at, PERMISSION.updated_at) AS info_data, 'PERMISSION' AS type_data, PERMISSION.id AS permission_id " +
            "FROM FUSION.\"GROUP\" AS \"GROUP\" " +
            "LEFT OUTER JOIN FUSION.GROUP_PERMISSION AS GROUP_PERMISSION ON GROUP_PERMISSION.group_id = \"GROUP\".id " +
            "LEFT OUTER JOIN FUSION.PERMISSION AS PERMISSION ON GROUP_PERMISSION.permission_id = PERMISSION.id " +
            "UNION ALL( " +
            "SELECT group_id, group_name, group_created_at, group_updated_at, " +
            "CONCAT_WS('||', permission_id, GROUP_CONCAT(content SEPARATOR ';'), code, language_id, label, GROUP_CONCAT(text_id SEPARATOR ';')) AS info_data, 'PERMISSION_LANG_VARIANT' AS type_data, permission_id " +
            "FROM ( " +
            "SELECT \"GROUP\".id AS group_id, PERMISSION.id AS permission_id, TEXT.content, LANG.code, TEXT.language_id, LANG.label, TEXT.id AS text_id, \"GROUP\".name AS group_name, \"GROUP\".created_at AS group_created_at, \"GROUP\".updated_at AS group_updated_at " +
            "FROM FUSION.\"GROUP\" AS \"GROUP\" " +
            "LEFT OUTER JOIN FUSION.GROUP_PERMISSION AS GROUP_PERMISSION ON GROUP_PERMISSION.group_id = \"GROUP\".id " +
            "LEFT OUTER JOIN FUSION.PERMISSION AS PERMISSION ON GROUP_PERMISSION.permission_id = PERMISSION.id " +
            "LEFT OUTER JOIN FUSION.TEXT AS TEXT ON TEXT.id = PERMISSION.label_text_id " +
            "LEFT OUTER JOIN FUSION.LANGUAGE AS LANG ON TEXT.language_id = LANG.id " +
            "UNION ALL " +
            "SELECT \"GROUP\".id AS group_id, PERMISSION.id AS permission_id, TEXT.content, LANG.code, TEXT.language_id, LANG.label, TEXT.id AS text_id, \"GROUP\".name AS group_name, \"GROUP\".created_at AS group_created_at, \"GROUP\".updated_at AS group_updated_at " +
            "FROM FUSION.\"GROUP\" AS \"GROUP\" " +
            "LEFT OUTER JOIN FUSION.GROUP_PERMISSION AS GROUP_PERMISSION ON GROUP_PERMISSION.group_id = \"GROUP\".id " +
            "LEFT OUTER JOIN FUSION.PERMISSION AS PERMISSION ON GROUP_PERMISSION.permission_id = PERMISSION.id " +
            "LEFT OUTER JOIN FUSION.TEXT AS TEXT ON TEXT.id = PERMISSION.description_text_id " +
            "LEFT OUTER JOIN FUSION.LANGUAGE AS LANG ON TEXT.language_id = LANG.id) " +
            "GROUP BY permission_id, language_id " +
            ")) " +
            "ORDER BY permission_id " +
            "))"
        var queryArgs: ListBuffer[String] = ListBuffer()
        var whereStatements: ListBuffer[String] = ListBuffer()
        queryFilters.filters.foreach({ filter =>
            var innerWhereStatement: ListBuffer[String] = ListBuffer()
            // manage ids search
            if (!filter.id.isEmpty) {
                innerWhereStatement += s"group_id in (${(for (i <- 1 to filter.id.length) yield "?").mkString(",")})"
                queryArgs ++= filter.id
            }
            // manage names search
            if (!filter.name.isEmpty) {
                innerWhereStatement += s"group_name in (${(for (i <- 1 to filter.name.length) yield "?").mkString(",")})"
                queryArgs ++= filter.name
            }
            // manage metadate search
            filter.createdAt match {
                case Some((test, time)) => {
                    innerWhereStatement += s"group_created_at ${
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
                    innerWhereStatement += s"group_updated_at ${
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
                s"group_${o._1} ${o._2 match {
                    case 1 => "ASC"
                    case -1 => "DESC"
                }}"
            ).mkString(", ")}"
        }
        makeQuery(queryString)
        .setParams(queryArgs.toList)
    }

    //Get existing Groups from database
    def getGroups(queryFilters: GroupStore.GetGroupsFilters)(implicit ec: ExecutionContext): Future[List[Group]] = {
        executeQuery(makeGroupsQuery(queryFilters)).transformWith({
            case Success(groupResults) => {
                // map each group from queryResult by grouping results by GROUP.id and mapping to group objects creation
                val groups = groupResults.toList.groupBy(_(0)).map(entityReflection => {
                    (for (
                        // Start a for comprehension
                        group <- Right(makeGroup
                            .setId(entityReflection._2(0)(0).toString)
                            .setName(entityReflection._2(0)(1).toString)
                            .setCreatedAt(entityReflection._2(0)(2) match {
                                case createdAt: Timestamp => createdAt
                                case _ => null
                            })
                            .setUpdatedAt(entityReflection._2(0)(3) match {
                                case updatedAt: Timestamp => updatedAt
                                case _ => null
                            })
                        ) flatMap { group =>
                            val groupedRows = getRelationsGroupedRowsFrom(entityReflection._2, 4, 5)
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
                                        ) yield group.addMember(profile))
                                    })
                                }
                                case None => {}
                            }
                            groupedRows.get("ORGANIZATION") match {
                                case Some(organizationReflections) => {
                                    organizationReflections.size match {
                                        case 0 => Future.failed(OrganizationNotFoundException(s"Group ${group.id}:${group.name} might be orphan"))
                                        case 1 => {
                                            val organizationReflection = organizationReflections.last
                                            organizationReflection.partition(_._1 == "ORGANIZATION") match {
                                                case result => {
                                                    result._1.length match {
                                                        case 0 => Future.failed(OrganizationNotFoundException(s"Group ${group.id}:${group.name} might be orphan"))
                                                        case 1 => {
                                                            val orgDef = result._1(0)._2
                                                            (for (
                                                                organization <- Right(new OrganizationStore()
                                                                    .makeOrganization
                                                                    .setId(orgDef(0))
                                                                    .setLabel(orgDef(1))
                                                                    .setCreatedAt(Timestamp.from(Instant.parse(orgDef(3))) match {
                                                                        case createdAt: Timestamp => createdAt
                                                                        case _ => null
                                                                    })
                                                                    .setUpdatedAt(Timestamp.from(Instant.parse(orgDef(4))) match {
                                                                        case updatedAt: Timestamp => updatedAt
                                                                        case _ => null
                                                                    })
                                                                ) flatMap { organization =>
                                                                    Try(orgDef(2).toBoolean) match {
                                                                        case Success(queryable) => {
                                                                            if (queryable) Right(organization.setQueryable)
                                                                            else Right(organization.setUnqueryable)
                                                                        }
                                                                        case Failure(cause) => Right(organization)
                                                                    }
                                                                } flatMap { organization =>
                                                                    result._2.partition(_._1 == "ORGTYPE") match {
                                                                        case result => {
                                                                            result._1.length match {
                                                                                case 0 => 
                                                                                case 1 => {
                                                                                    val orgTypeDef = result._1(0)._2
                                                                                    val orgType = new OrganizationTypeStore()
                                                                                    .makeOrganizationType
                                                                                    .setId(orgTypeDef(1))
                                                                                    .setLabelTextId(orgTypeDef(2))
                                                                                    .setCreatedAt(Try(orgTypeDef(3).asInstanceOf[Timestamp]) match {
                                                                                        case Success(createdAt) => createdAt
                                                                                        case _ => null
                                                                                    })
                                                                                    .setUpdatedAt(Try(orgTypeDef(4).asInstanceOf[Timestamp]) match {
                                                                                        case Success(updatedAt) => updatedAt
                                                                                        case _ => null
                                                                                    })
                                                                                    result._2.foreach({ result =>
                                                                                        val orgTypeLangVariantDef = result._2
                                                                                        orgType.setLabel(
                                                                                            Language.apply
                                                                                            .setId(orgTypeLangVariantDef(4))
                                                                                            .setCode(orgTypeLangVariantDef(3))
                                                                                            .setLabel(orgTypeLangVariantDef(5)),
                                                                                            orgTypeLangVariantDef(2)
                                                                                        )
                                                                                    })
                                                                                    organization.setType(orgType)
                                                                                }
                                                                                case _ => 
                                                                            }
                                                                        }
                                                                    }
                                                                    Right(organization)
                                                                }
                                                            ) yield group.setRelatedOrganization(organization))
                                                        }
                                                        case _ => Future.failed(DuplicateOrganizationException(s"Filesystem ${group.id}:${group.name} has duplicate organization relation"))
                                                    }
                                                }
                                            }
                                        }
                                        case _ => Future.failed(DuplicateOrganizationException(s"Group ${group.id}:${group.name} has duplicate organization relation"))
                                    }
                                }
                                case None => {}
                            }
                            groupedRows.get("PERMISSION") match {
                                case Some(permissionReflections) => {
                                    permissionReflections.foreach({ permissionReflection =>
                                        permissionReflection.partition(_._1 == "PERMISSION") match {
                                            case result => {
                                                result._1.length match {
                                                    case 0 => 
                                                    case _ => {
                                                        val permissionDef = result._1(0)._2
                                                        (for (
                                                            permission <- Right(new PermissionStore()
                                                                .makePermission
                                                                .setId(permissionDef(0))
                                                                .setKey(permissionDef(1))
                                                                .setLabelTextId(permissionDef(2))
                                                                .setDescriptionTextId(permissionDef(3))
                                                                .setRelatedApplication(new ApplicationStore()
                                                                    .makeApplication
                                                                    .setId(permissionDef(5))
                                                                )
                                                                .setCreatedAt(Timestamp.from(Instant.parse(permissionDef(6))) match {
                                                                    case createdAt: Timestamp => createdAt
                                                                    case _ => null
                                                                })
                                                                .setUpdatedAt(Timestamp.from(Instant.parse(permissionDef(7))) match {
                                                                    case updatedAt: Timestamp => updatedAt
                                                                    case _ => null
                                                                })
                                                            ) flatMap { permission =>
                                                                Try(permissionDef(4).toBoolean) match {
                                                                    case Success(editable) => {
                                                                        if (editable) Right(permission.setEditable)
                                                                        else Right(permission.setReadonly)
                                                                    }
                                                                    case Failure(cause) => Right(permission)
                                                                }
                                                            } flatMap { permission =>
                                                                result._2.foreach({ result =>
                                                                    val permissionLangVariantDef = result._2
                                                                    .updated(1, result._2(1).split(";"))
                                                                    .updated(5, result._2(5).split(";"))

                                                                    permission.setLabel(
                                                                        Language.apply
                                                                        .setId(permissionLangVariantDef(3).asInstanceOf[String])
                                                                        .setCode(permissionLangVariantDef(2).asInstanceOf[String])
                                                                        .setLabel(permissionLangVariantDef(1).asInstanceOf[String]),
                                                                        permissionLangVariantDef(2).asInstanceOf[Array[String]](0)
                                                                    )

                                                                    permission.setDescription(
                                                                        Language.apply
                                                                        .setId(permissionLangVariantDef(3).asInstanceOf[String])
                                                                        .setCode(permissionLangVariantDef(2).asInstanceOf[String])
                                                                        .setLabel(permissionLangVariantDef(1).asInstanceOf[String]),
                                                                        permissionLangVariantDef(2).asInstanceOf[Array[String]](0)
                                                                    )
                                                                })
                                                                Right(permission)
                                                            }
                                                        ) yield group.addPermission(permission) )
                                                    }
                                                }
                                            }
                                        }
                                    })
                                }
                                case None => {}
                            }
                            Right(group)
                        }
                    ) yield group).getOrElse(null)
                })
                Future.successful(groups.toList)
            }
            case Failure(cause) => Future.failed(GroupQueryExecutionException(cause))
        })
    }

    def getAllGroups(implicit ec: ExecutionContext): Future[List[Group]] = {
        getGroups(GroupStore.GetGroupsFilters().copy(
            orderBy = List(
                ("id", 1)
            )
        )).transformWith({
            case Success(groups) =>
                groups.length match {
                    case 0 => Future.failed(new NoEntryException("Group store is empty"))
                    case _ => Future.successful(groups)
                }
            case Failure(cause) => Future.failed(cause)
        })
    }

    def getGroupById(id: String)(implicit ec: ExecutionContext): Future[Group] = {
        getGroups(
            GroupStore.GetGroupsFilters().copy(
                filters = List(
                    GroupStore.GetGroupsFilter().copy(
                        id = List(id)
                    )
                )
            )
        ).transformWith({
            case Success(groups) => 
                groups.length match {
                    case 0 => Future.failed(new GroupNotFoundException(s"Group ${id} couldn't be found"))
                    case 1 => Future.successful(groups(0))
                    case _ => Future.failed(new DuplicateGroupException)
                }
            case Failure(cause) => Future.failed(cause)
        })
    }

    def persistGroup(group: Group)(implicit ec: ExecutionContext): Future[Unit] = {
        val transaction = makeTransaction
        transaction match {
            case Success(tx) => {
                val relationCache: IgniteCache[String, GroupPermission] =
                    wrapper.getCache[String, GroupPermission](cache)
                Future.sequence(
                    List(
                        // Save entity
                        Utils.igniteToScalaFuture(igniteCache.putAsync(
                            group.id, group
                        )),
                        // Save permissions
                        Utils.igniteToScalaFuture(relationCache.putAllAsync(
                            group.permissions
                            .filter(_._1 == true)
                            .map({ org =>
                                (
                                    group.id +":"+ org._2.id,
                                    GroupPermission(
                                        group.id,
                                        org._2.id
                                    )
                                )
                            }).toMap[String, GroupPermission].asJava
                        )),
                        // Remove permissions
                        Utils.igniteToScalaFuture(relationCache.removeAllAsync(
                            group.permissions
                            .filter(_._1 == false)
                            .map(group.id +":"+ _._2.id).toSet[String].asJava
                        ))
                        // TODO Relations
                    )
                ).transformWith({
                    case Success(value) => {
                        commitTransaction(transaction).transformWith({
                            case Success(value) => Future.unit
                            case Failure(cause) => Future.failed(GroupNotPersistedException(cause))
                        })
                    }
                    case Failure(cause) => {
                        rollbackTransaction(transaction)
                        Future.failed(GroupNotPersistedException(cause))
                    }
                })
            }
            case Failure(cause) => Future.failed(GroupNotPersistedException(cause))
        }
    }

    def bulkPersistGroups(groups: List[Group])(implicit ec: ExecutionContext): Future[Unit] = {
        val transaction = makeTransaction
        transaction match {
            case Success(tx) => {
                val relationCache: IgniteCache[String, GroupPermission] =
                    wrapper.getCache[String, GroupPermission](cache)
                Future.sequence(
                    List(
                        Utils.igniteToScalaFuture(igniteCache.putAllAsync(
                            (groups.map(_.id) zip groups).toMap[UUID, Group].asJava
                        ))
                    )
                ).transformWith({
                    case Success(value) => {
                        commitTransaction(transaction).transformWith({
                            case Success(value) => Future.unit
                            case Failure(cause) => Future.failed(GroupNotPersistedException(cause))
                        })
                    }
                    case Failure(cause) => {
                        rollbackTransaction(transaction)
                        Future.failed(GroupNotPersistedException(cause))
                    }
                })
            }
            case Failure(cause) => Future.failed(GroupNotPersistedException(cause))
        }
    }

    def deleteGroup(group: Group)(implicit ec: ExecutionContext): Future[Unit] = {
        val transaction = makeTransaction
        transaction match {
            case Success(tx) => {
                val relationCache: IgniteCache[String, GroupPermission] =
                    wrapper.getCache[String, GroupPermission](cache)
                val profileStore = new ProfileStore()
                val organizationStore = new OrganizationStore()
                Future.sequence(
                    List(
                        Utils.igniteToScalaFuture(igniteCache.removeAsync(group.id)),
                        // Delete relations
                        // Delete permission relations
                        Utils.igniteToScalaFuture(relationCache.removeAllAsync(
                            group.permissions
                            .map({ org =>
                                group.id +":"+ org._2.id
                            }).toSet[String].asJava
                        )),
                        // Delete member (Profile) relations
                        profileStore
                            .getProfiles(
                                ProfileStore.GetProfilesFilters().copy(filters = List(
                                    ProfileStore.GetProfilesFilter().copy(id = group.members.map(_._2.id.toString))
                                ))
                            ).transformWith({
                                case Success(profiles) => {
                                    profileStore.bulkPersistProfiles(profiles.map(_.removeGroup(group)))
                                }
                                case Failure(cause) => Future.failed(cause)
                            }),
                        // Delete organization relation
                        organizationStore
                            .getOrganizationById(
                                group.relatedOrganization match {
                                    case Some(org) => org.id.toString
                                    case None => throw OrganizationNotFoundException("Specified group is organization orphan")
                                }
                            ).transformWith({
                                case Success(organization) => {
                                    organization.removeRelatedGroup(group).persist
                                }
                                case Failure(cause) => Future.failed(cause)
                            })
                    )
                ).transformWith({
                    case Success(value) => {
                        commitTransaction(transaction).transformWith({
                            case Success(value) => Future.unit
                            case Failure(cause) => Future.failed(GroupNotPersistedException(cause))
                        })
                    }
                    case Failure(cause) => {
                        rollbackTransaction(transaction)
                        Future.failed(GroupNotPersistedException(cause))
                    }
                })
            }
            case Failure(cause) => Future.failed(GroupNotPersistedException(cause))
        }
    }
}

object GroupStore {
    case class GetGroupsFilter(
        id: List[String] = List(),
        name: List[String] = List(),
        createdAt: Option[(String, Timestamp)] = None, // (date, (eq, lt, gt, ne))
        updatedAt: Option[(String, Timestamp)] = None // (date, (eq, lt, gt, ne))
    )
    case class GetGroupsFilters(
        filters: List[GetGroupsFilter] = List(),
        orderBy: List[(String, Int)] = List() // (column, direction)
    ) extends GetEntityFilters
}
