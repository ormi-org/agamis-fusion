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
            ""
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

    // Get existing Groups from database
    // def getGroups(queryFilters: GroupStore.GetGroupsFilters)(implicit ec: ExecutionContext): Future[List[Group]] = {
    //     executeQuery(makeGroupsQuery(queryFilters)).transformWith({
    //         case Success(groupResults) => {
    //             // map each group from queryResult by grouping results by GROUP.id and mapping to group objects creation
    //             val groups = groupResults.toList.groupBy(_(0)).map(entityReflection => {
    //                 (for (
    //                     // Start a for comprehension
    //                     group <- Right(makeGroup
    //                         .setId(entityReflection._2(0)(0).toString)
    //                         .setName(entityReflection._2(0)(1).toString)
    //                         .setCreatedAt(entityReflection._2(0)(2) match {
    //                             case createdAt: Timestamp => createdAt
    //                             case _ => null
    //                         })
    //                         .setUpdatedAt(entityReflection._2(0)(3) match {
    //                             case updatedAt: Timestamp => updatedAt
    //                             case _ => null
    //                         })
    //                     ) flatMap { group =>
    //                         val 
    //                     }
    //                 ))
    //             })
    //         }
    //         case Failure(cause) => Future.failed(GroupQueryExecutionException(cause))
    //     })
    // }

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
