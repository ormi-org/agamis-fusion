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
        var baseQueryString = queryString.replace("$schema", schema)
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
            baseQueryString += " WHERE " + whereStatements.reverse.mkString(" OR ")
        }
        // manage order
        if (!queryFilters.orderBy.isEmpty) {
            baseQueryString += s" ORDER BY ${queryFilters.orderBy.map( o =>
                s"group_${o._1} ${o._2 match {
                    case 1 => "ASC"
                    case -1 => "DESC"
                }}"
            ).mkString(", ")}"
        }
        makeQuery(baseQueryString)
        .setParams(queryArgs.toList)
    }

    //Get existing Groups from database
    def getGroups(queryFilters: GroupStore.GetGroupsFilters)(implicit ec: ExecutionContext): Future[List[Group]] = {
        executeQuery(makeGroupsQuery(queryFilters)).transformWith({
            case Success(rows) => {
                // map each group from queryResult by grouping results by GROUP.id and mapping to group objects creation
                val entityReflections = rows.groupBy(_(0))
                val groups = rows.map(_(0)).distinct.map(entityReflections.get(_).get).map(entityReflection => {
                    val groupedRows = getRelationsGroupedRowsFrom(entityReflection, 4, 5)
                    groupedRows.get("GROUP") match {
                        case Some(groupReflections) => {
                            val groupDef = groupReflections.head.head._2
                            (for (
                                // Start a for comprehension
                                group <- Right(makeGroup
                                    .setId(groupDef(0).toString)
                                    .setName(groupDef(1).toString)
                                    .setCreatedAt(Utils.timestampFromString(groupDef(2)) match {
                                        case createdAt: Timestamp => createdAt
                                        case _ => null
                                    })
                                    .setUpdatedAt(Utils.timestampFromString(groupDef(3)) match {
                                        case updatedAt: Timestamp => updatedAt
                                        case _ => null
                                    })
                                ) flatMap { group =>
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
                                                                            .setCreatedAt(Utils.timestampFromString(orgDef(3)) match {
                                                                                case createdAt: Timestamp => createdAt
                                                                                case _ => null
                                                                            })
                                                                            .setUpdatedAt(Utils.timestampFromString(orgDef(4)) match {
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
                                                                                            .setCreatedAt(Utils.timestampFromString(orgTypeDef(3)) match {
                                                                                                case createdAt: Timestamp => createdAt
                                                                                                case _ => null
                                                                                            })
                                                                                            .setUpdatedAt(Utils.timestampFromString(orgTypeDef(4)) match {
                                                                                                case updatedAt: Timestamp => updatedAt
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
                                                                case _ => Future.failed(DuplicateOrganizationException(s"Group ${group.id}:${group.name} has duplicate organization relation"))
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
                                                                        .setCreatedAt(Utils.timestampFromString(permissionDef(6)) match {
                                                                            case createdAt: Timestamp => createdAt
                                                                            case _ => null
                                                                        })
                                                                        .setUpdatedAt(Utils.timestampFromString(permissionDef(7)) match {
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
                                                                        result._2.partition(_._1 == "PERMISSION_LABEL_LANG_VARIANT") match {
                                                                            case result => {
                                                                                result._1.length match {
                                                                                    case 0 =>
                                                                                    case _ => {
                                                                                        result._1.foreach({ labelLangVariant =>
                                                                                            val variantDef = labelLangVariant._2
                                                                                            permission.setLabel(
                                                                                                Language.apply
                                                                                                .setId(variantDef(3).toString)
                                                                                                .setCode(variantDef(2).toString)
                                                                                                .setLabel(variantDef(4).toString),
                                                                                                variantDef(1).toString
                                                                                            )
                                                                                        })
                                                                                        result._2.filter(_._1 == "PERMISSION_DESC_LANG_VARIANT").foreach({ labelLangVariant =>
                                                                                            val variantDef = labelLangVariant._2
                                                                                            permission.setDescription(
                                                                                                Language.apply
                                                                                                .setId(variantDef(3).toString)
                                                                                                .setCode(variantDef(2).toString)
                                                                                                .setLabel(variantDef(4).toString),
                                                                                                variantDef(1).toString
                                                                                            )
                                                                                        })
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
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
                        }
                        case None => {}
                    }
                })
                Future.successful(groups.toList.asInstanceOf[List[Group]])
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
                    case 1 => Future.successful(groups.head)
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
