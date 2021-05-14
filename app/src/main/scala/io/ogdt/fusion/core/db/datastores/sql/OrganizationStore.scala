package io.ogdt.fusion.core.db.datastores.sql

import io.ogdt.fusion.core.db.models.sql.Organization
import io.ogdt.fusion.core.db.datastores.typed.SqlMutableStore
import io.ogdt.fusion.core.db.datastores.typed.sql.GetEntityFilters
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
import org.apache.ignite.cache.CacheMode
import io.ogdt.fusion.core.db.datastores.typed.sql.SqlStoreQuery
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext

class OrganizationStore(implicit wrapper: IgniteClientNodeWrapper) extends SqlMutableStore[UUID, Organization] {

    override val schema: String = "FUSION"
    override val cache: String = s"SQL_${schema}_ORGANIZATION"
    override protected var igniteCache: IgniteCache[UUID, Organization] = wrapper.cacheExists(cache) match {
        case true => wrapper.getCache[UUID, Organization](cache)
        case false => {
            wrapper.createCache[UUID, Organization](
                wrapper.makeCacheConfig[UUID, Organization]
                .setCacheMode(CacheMode.REPLICATED)
                .setDataRegionName("Fusion")
                // .setQueryEntities(
                //     List(
                //         new QueryEntity(classTag[UUID].runtimeClass, classTag[FilesystemOrganization].runtimeClass)
                //     ).asJava
                // )
                .setName(cache)
                .setSqlSchema(schema)
                .setIndexedTypes(classOf[UUID], classOf[Organization])
            )
        }
    }

    // Create and get new Organization Object
    def makeOrganization: Organization = {
        implicit val organizationStore: OrganizationStore = this
        new Organization
    }

    def makeOrganizationsQuery(queryFilters: OrganizationStore.GetOrganizationsFilters): SqlStoreQuery = {
        var queryString: String = 
            "SELECT id, label, type, queryable, created_at, updated_at, data, data_type " +
            "FROM " +
            "(SELECT ORG.id, ORG.label, type, queryable, ORG.created_at, ORG.updated_at, " +
            "CONCAT_WS('||', PROFILE.id, lastname, firstname, last_login, is_active, user_id, PROFILE.organization_id, PROFILE.created_at, PROFILE.updated_at) AS data, 'PROFILE' AS data_type " +
            s"FROM $schema.ORGANIZATION as ORG " +
            s"INNER JOIN $schema.PROFILE AS PROFILE ON PROFILE.organization_id = ORG.id " +
            "UNION ALL " +
            "SELECT ORG.id, ORG.label, type, queryable, ORG.created_at, ORG.updated_at, " +
            "CONCAT_WS('||', FS.id, rootdir_id, FS.label, shared, FS_ORG.is_default, FS.created_at, FS.updated_at) AS data, 'FS' AS data_type " +
            s"FROM $schema.ORGANIZATION as ORG " +
            s"LEFT OUTER JOIN $schema.FILESYSTEM_ORGANIZATION AS FS_ORG ON FS_ORG.organization_id = ORG.id " +
            s"LEFT OUTER JOIN $schema.FILESYSTEM AS FS ON FS_ORG.filesystem_id = FS.id)"
        var queryArgs: ListBuffer[String] = ListBuffer()
        var whereStatements: ListBuffer[String] = ListBuffer()
        queryFilters.filters.foreach({ filter =>
            var innerWhereStatement: ListBuffer[String] = ListBuffer()
            // manage ids search
            if (filter.id.length > 0) {
                innerWhereStatement += s"ORG.id in (${(for (i <- 1 to filter.id.length) yield "?").mkString(",")})"
                queryArgs ++= filter.id
            }
            // manage labels search
            if (filter.label.length > 0) {
                innerWhereStatement += s"ORG.label in (${(for (i <- 1 to filter.label.length) yield "?").mkString(",")})"
                queryArgs ++= filter.label
            }
            // manage types search
            if (filter.`type`.length > 0) {
                innerWhereStatement += s"ORG.type in (${(for (i <- 1 to filter.`type`.length) yield "?").mkString(",")})"
                queryArgs ++= filter.`type`
            }
            // manage shared state search
            filter.queryable match {
                case Some(value) => {
                    innerWhereStatement += s"ORG.queryable = ?"
                    queryArgs += value.toString
                }
                case None => ()
            }
            // manage metadate search
            filter.createdAt match {
                case Some((test, time)) => {
                    innerWhereStatement += s"ORG.created_at ${
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
                    innerWhereStatement += s"ORG.updated_at ${
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
                s"ORG.${o._1} ${o._2 match {
                    case 1 => "ASC"
                    case -1 => "DESC"
                }}"
            ).mkString(", ")}"
        }
        makeQuery(queryString)
        .setParams(queryArgs.toList)
    }

    // def getOrganizationById(id: String): Future[Organization] = {
    //     executeQuery(
    //         makeQuery(
    //             "SELECT USER.id, username, password, PROFILE.id, lastname, firstname, last_login " +
    //             s"FROM $schema.USER as USER " +
    //             s"INNER JOIN $schema.PROFILE as PROFILE ON PROFILE.user_id = USER.id " +
    //             "WHERE USER.id = ?")
    //         .setParams(List(id))
    //     ).transformWith({
    //         case Success(userResults) => {
    //             var row = userResults(0)
    //             Future.successful(
    //                 (for (
    //                     user <- Right(
    //                         makeOrganization
    //                         .setId(row(0).toString)
    //                         .setOrganizationname(row(1).toString)
    //                         .setPassword(row(2).toString)
    //                     ) flatMap { user => 
    //                         if (row(3) != null && row(4) != null && row(5) != null && row(6) != null)
    //                             Right(user.addRelatedProfile(
    //                                 new ProfileStore().makeProfile
    //                                 .setId(row(3).toString)
    //                                 .setLastname(row(4).toString)
    //                                 .setFirstname(row(5).toString)
    //                                 .setLastLogin(row(6) match {
    //                                     case lastlogin: Timestamp => lastlogin
    //                                     case _ => null
    //                                 })
    //                             ))
    //                         else Right(user)
    //                     }
    //                 ) yield user)
    //                 .getOrElse(null))
    //         }
    //         case Failure(cause) => Future.failed(new Exception("bla bla bla",cause)) // TODO : changer pour une custom
    //     })
    // }

    // Get existing organizations from database
    // def getOrganizations(queryFilters: OrganizationStore.GetOrganizationsFilters)(implicit ec: ExecutionContext): Future[List[Organization]] = {
    //     executeQuery(
    //         makeOrganizationsQuery(queryFilters)
    //     ).transformWith({
    //         case Success(organizationResults) => {
    //             var organizations = organizationResults.par map(row => {
    //                 (for (
    //                     organization <- Right(
    //                         makeOrganization
    //                         .setId(row(0).toString)
    //                         .setLabel(row(1).toString)
    //                         .setType(row(2).toString)
    //                     ) flatMap { user => 
    //                         if (row(3) != null && row(4) != null && row(5) != null && row(6) != null)
    //                             Right(user.addRelatedProfile(
    //                                 new ProfileStore().makeProfile
    //                                 .setId(row(3).toString)
    //                                 .setLastname(row(4).toString)
    //                                 .setFirstname(row(5).toString)
    //                                 .setLastLogin(row(6) match {
    //                                     case lastlogin: Timestamp => lastlogin
    //                                     case _ => null
    //                                 })
    //                             ))
    //                         else Right(user)
    //                     }
    //                 ) yield organization)
    //                 .getOrElse(null)
    //             })
    //             Future.successful(users.toList)
    //         }
    //         case Failure(cause) => throw cause
    //     })
    // }

    // Save organization object's modification to database
    def persistOrganization(organization: Organization): Future[Unit] = {
        Utils.igniteToScalaFuture(igniteCache.putAsync(
            organization.id, organization
        )).transformWith({
            case Success(value) => Future.successful()
            case Failure(cause) => Future.failed(new Exception("bla bla bla",cause)) // TODO : changer pour une custom
        })
    }

    /** A result of bulkPersistOrganizations method
      * 
      * @constructor create a new BulkPersistOrganizationsResult with a count of inserted Organizations and a list of errors
      * @param inserts a count of the effectively inserted Organizations
      * @param errors a list of errors catched from a file deletion
      */
    case class BulkPersistOrganizationsResult(inserts: Int, errors: List[String])

    // Save several object's modifications
    def bulkPersistOrganizations(organizations: List[Organization]): Future[BulkPersistOrganizationsResult] = {
        Utils.igniteToScalaFuture(igniteCache.putAllAsync(
            (organizations.map(_.id) zip organizations).toMap[UUID, Organization].asJava
        )).transformWith({
            case Success(value) => {
                Future.sequence(
                    organizations.map(organization => Utils.igniteToScalaFuture(igniteCache.containsKeyAsync(organization.id)))
                ).map(lookup => (organizations zip lookup))
                .transformWith(lookup => {
                    Future.successful(BulkPersistOrganizationsResult(
                        lookup.get.filter(_._2 == true).length,
                        lookup.get.filter(_._2 == false).map("Insert organization "+_._1.toString+" failed")
                    ))
                })
            }
            case Failure(cause) => Future.failed(new Exception("bla bla bla",cause)) // TODO : changer pour une custom
        })
    }

    // Delete user from database
    def deleteOrganization(organization: Organization): Future[Unit] = {
        organization.relatedProfiles.map(p => p)
        Utils.igniteToScalaFuture(igniteCache.removeAsync(organization.id))
        .transformWith({
            case Success(value) => Future.successful()
            case Failure(cause) => Future.failed(new Exception("bla bla bla",cause)) // TODO : changer pour une custom
        })
    }

    /** A result of bulkDeleteOrganizations method
      * 
      * @constructor create a new BulkDeleteOrganizationsResult with a count of deleted Organizations and a list of errors
      * @param inserts a count of the effectively deleted Organizations
      * @param errors a list of errors catched from an organization deletion
      */
    case class BulkDeleteOrganizationsResult(inserts: Int, errors: List[String])

    // Delete several users from database
    def bulkDeleteOrganizations(organizations: List[Organization]): Future[BulkDeleteOrganizationsResult] = {
        Utils.igniteToScalaFuture(igniteCache.removeAllAsync(organizations.map(_.id).toSet.asJava))
        .transformWith({
            case Success(value) => {
                Future.sequence(
                    organizations.map(profile => Utils.igniteToScalaFuture(igniteCache.containsKeyAsync(profile.id)))
                ).map(lookup => (organizations zip lookup))
                .transformWith(lookup => {
                    Future.successful(BulkDeleteOrganizationsResult(
                        lookup.get.filter(_._2 == false).length,
                        lookup.get.filter(_._2 == true).map("Failed to delete organization "+_._1.toString)
                    ))
                })
            }
            case Failure(cause) => Future.failed(new Exception("bla bla bla",cause)) // TODO : changer pour une custom
        })
    }
}

object OrganizationStore {
    case class GetOrganizationsFilter(
        id: List[String],
        label: List[String],
        `type`: List[String],
        queryable: Option[Boolean],
        createdAt: Option[(String, Timestamp)], // (date, (eq, lt, gt, ne))
        updatedAt: Option[(String, Timestamp)], // (date, (eq, lt, gt, ne))
    )
    case class GetOrganizationsFilters(
        filters: List[GetOrganizationsFilter],
        orderBy: List[(String, Int)]
    ) extends GetEntityFilters
}