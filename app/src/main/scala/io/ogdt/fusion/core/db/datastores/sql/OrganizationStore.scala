package io.ogdt.fusion.core.db.datastores.sql

import io.ogdt.fusion.core.db.models.sql.Organization
import io.ogdt.fusion.core.db.datastores.typed.SqlMutableStore
import io.ogdt.fusion.core.db.datastores.typed.sql.GetEntityFilters
import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper

import org.apache.ignite.IgniteCache

import scala.util.Success
import scala.util.Failure

import scala.concurrent.Future
import java.sql.Timestamp
import java.util.UUID
import scala.jdk.CollectionConverters._
import io.ogdt.fusion.core.db.common.Utils
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.cache.CacheAtomicityMode
import io.ogdt.fusion.core.db.datastores.typed.sql.SqlStoreQuery
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext
import scala.util.Try
import io.ogdt.fusion.core.db.models.sql.generics.Text
import io.ogdt.fusion.core.db.models.sql.generics.Language
import io.ogdt.fusion.core.db.models.sql.OrganizationType

class OrganizationStore(implicit wrapper: IgniteClientNodeWrapper) extends SqlMutableStore[UUID, Organization] {

    override val schema: String = "FUSION"
    override val cache: String = s"SQL_${schema}_ORGANIZATION"
    override protected var igniteCache: IgniteCache[UUID, Organization] = wrapper.cacheExists(cache) match {
        case true => wrapper.getCache[UUID, Organization](cache)
        case false => {
            wrapper.createCache[UUID, Organization](
                wrapper.makeCacheConfig[UUID, Organization]
                .setCacheMode(CacheMode.REPLICATED)
                .setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL)
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
            "SELECT org_id, org_label, org_queryable, org_created_at, org_updated_at, info_data, type_data " +
            "FROM " +
            "(SELECT ORG.id AS org_id, ORG.label AS org_label, queryable AS org_queryable, ORG.created_at AS org_created_at, ORG.updated_at AS org_updated_at, " +
            "CONCAT_WS('||', PROFILE.id, lastname, firstname, last_login, is_active, user_id, PROFILE.organization_id, PROFILE.created_at, PROFILE.updated_at) AS data, 'PROFILE' AS data_type " +
            s"FROM $schema.ORGANIZATION as ORG " +
            s"INNER JOIN $schema.PROFILE AS PROFILE ON PROFILE.organization_id = ORG.id " +
            "UNION ALL " +
            "SELECT ORG.id AS org_id, ORG.label AS org_label, queryable AS org_queryable, ORG.created_at AS org_created_at, ORG.updated_at AS org_updated_at, " +
            "CONCAT_WS('||', FS.id, rootdir_id, FS.label, shared, FS_ORG.is_default, FS.created_at, FS.updated_at) AS data, 'FS' AS data_type " +
            s"FROM $schema.ORGANIZATION as ORG " +
            s"LEFT OUTER JOIN $schema.FILESYSTEM_ORGANIZATION AS FS_ORG ON FS_ORG.organization_id = ORG.id " +
            s"LEFT OUTER JOIN $schema.FILESYSTEM AS FS ON FS_ORG.filesystem_id = FS.id" +
            "UNION ALL " +
            "SELECT ORG.id AS org_id, ORG.label AS org_label, queryable AS org_queryable, ORG.created_at AS org_created_at, ORG.updated_at AS org_updated_at, " +
            "CONCAT_WS('||', ORGTYPE.id, TEXT.content, LANG.code, LANG.label, TEXT.language_id, TEXT.id, ORGTYPE.created_at, ORGTYPE.updated_at) AS info_data, 'ORGTYPE_LANG_VARIANT' AS type_data " +
	        s"FROM $schema.ORGANIZATION as ORG " +
	        s"LEFT OUTER JOIN $schema.ORGANIZATIONTYPE AS ORGTYPE ON ORGTYPE.id = ORG.organizationtype_id " +
	        s"LEFT OUTER JOIN $schema.TEXT AS TEXT ON TEXT.id = ORGTYPE.label_text_id " +
	        s"LEFT OUTER JOIN $schema.LANGUAGE AS LANG ON LANG.id = TEXT.language_id)"
        var queryArgs: ListBuffer[String] = ListBuffer()
        var whereStatements: ListBuffer[String] = ListBuffer()
        queryFilters.filters.foreach({ filter =>
            var innerWhereStatement: ListBuffer[String] = ListBuffer()
            // manage ids search
            if (filter.id.length > 0) {
                innerWhereStatement += s"org_id in (${(for (i <- 1 to filter.id.length) yield "?").mkString(",")})"
                queryArgs ++= filter.id
            }
            // manage labels search
            if (filter.label.length > 0) {
                innerWhereStatement += s"org_label in (${(for (i <- 1 to filter.label.length) yield "?").mkString(",")})"
                queryArgs ++= filter.label
            }
            // manage types search
            if (filter.`type`.length > 0) {
                innerWhereStatement += s"org_type in (${(for (i <- 1 to filter.`type`.length) yield "?").mkString(",")})"
                queryArgs ++= filter.`type`
            }
            // manage shared state search
            filter.queryable match {
                case Some(value) => {
                    innerWhereStatement += s"queryable = ?"
                    queryArgs += value.toString
                }
                case None => ()
            }
            // manage metadate search
            filter.createdAt match {
                case Some((test, time)) => {
                    innerWhereStatement += s"org_created_at ${
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
                    innerWhereStatement += s"org_updated_at ${
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
                s"org_${o._1} ${o._2 match {
                    case 1 => "ASC"
                    case -1 => "DESC"
                }}"
            ).mkString(", ")}"
        }
        makeQuery(queryString)
        .setParams(queryArgs.toList)
    }

    // Get existing organizations from database
    def getOrganizations(queryFilters: OrganizationStore.GetOrganizationsFilters)(implicit ec: ExecutionContext): Future[List[Organization]] = {
        executeQuery(
            makeOrganizationsQuery(queryFilters)
        ).transformWith({
            case Success(organizationResults) => {
                var organizations = organizationResults.toList.groupBy(_(0)).map(entityReflection => {
                    (for (
                        organization <- Right(
                            makeOrganization
                            .setId(entityReflection._2(0)(0).toString)
                            .setLabel(entityReflection._2(0)(1).toString)
                            .setCreatedAt(entityReflection._2(0)(3) match {
                                case createdAt: Timestamp => createdAt
                                case _ => null
                            })
                            .setUpdatedAt(entityReflection._2(0)(4) match {
                                case updatedAt: Timestamp => updatedAt
                                case _ => null
                            })
                        ) flatMap { organization => 
                            entityReflection._2(0)(2) match {
                                case queryable: Boolean => {
                                    if (queryable) Right(organization.setQueryable)
                                    else Right(organization.setUnqueryable)
                                }
                                case _ => Right(organization)
                            }
                        } flatMap { organization =>
                            var orgType: Option[OrganizationType] = None
                            entityReflection._2.foreach({ relation =>
                                relation(6) match {
                                    case "FS" => {
                                        val fileSystemReflection = relation(5).asInstanceOf[String].split("||")
                                        if (fileSystemReflection.length == 7) {
                                            (for (
                                                fileSystem <- Right(
                                                    new FileSystemStore().makeFileSystem
                                                    .setId(fileSystemReflection(0))
                                                    .setRootdirId(fileSystemReflection(1))
                                                    .setCreatedAt(Try(fileSystemReflection(5).asInstanceOf[Timestamp]) match {
                                                        case Success(createdAt) => createdAt
                                                        case _ => null 
                                                    })
                                                    .setUpdatedAt(Try(fileSystemReflection(6).asInstanceOf[Timestamp]) match {
                                                        case Success(updatedAt) => updatedAt
                                                        case _ => null
                                                    })
                                                ) flatMap { fileSystem =>
                                                    Try(fileSystemReflection(3).asInstanceOf[Boolean]) match {
                                                        case Success(shared) => {
                                                            if (shared) Right(fileSystem.setShared)
                                                            else Right(fileSystem.setUnshared)
                                                        }
                                                        case _ => Right(fileSystem)
                                                    }
                                                } 
                                            ) yield {
                                                if (fileSystemReflection(4) == true) {
                                                    organization.setDefaultFileSystem(fileSystem)
                                                } else {
                                                    organization.addFileSystem(fileSystem)
                                                }
                                            })
                                        }
                                    }
                                    case "PROFILE" => {
                                        val profileReflection = relation(5).asInstanceOf[String].split("||")
                                        if (profileReflection.length == 9) {
                                            (for (
                                                profile <- Right(
                                                    new ProfileStore().makeProfile
                                                    .setId(profileReflection(0))
                                                    .setLastname(profileReflection(1))
                                                    .setFirstname(profileReflection(2))
                                                    .setLastLogin(Try(profileReflection(3).asInstanceOf[Timestamp]) match {
                                                        case Success(lastlogin) => lastlogin
                                                        case _ => null
                                                    })
                                                    .setCreatedAt(Try(profileReflection(7).asInstanceOf[Timestamp]) match {
                                                        case Success(createdAt) => createdAt
                                                        case _ => null
                                                    })
                                                    .setUpdatedAt(Try(profileReflection(8).asInstanceOf[Timestamp]) match {
                                                        case Success(updatedAt) => updatedAt
                                                        case _ => null
                                                    })
                                                ) flatMap { profile =>
                                                    Try(profileReflection(4).asInstanceOf[Boolean]) match {
                                                        case Success(isActive) => {
                                                            if (isActive) Right(profile.setActive)
                                                            else Right(profile.setInactive)
                                                        }
                                                        case _ => Right(profile)
                                                    }
                                                }
                                            ) yield {
                                                organization.addRelatedProfile(profile)
                                            })
                                        }
                                    }
                                    case "ORGTYPE_LANG_VARIANT" => {
                                        val orgTypeLangVariantReflection = relation(5).asInstanceOf[String].split("||")
                                        if (orgTypeLangVariantReflection.length == 7) {
                                            orgType match {
                                                case Some(value) => {
                                                    orgType = Some(value.setLabel(
                                                        Language.apply
                                                        .setId(orgTypeLangVariantReflection(3).toString)
                                                        .setCode(orgTypeLangVariantReflection(2).toString)
                                                        .setLabel(orgTypeLangVariantReflection(4).toString),
                                                        orgTypeLangVariantReflection(1).toString
                                                    ))
                                                }
                                                case None => {
                                                    orgType = Some(new OrganizationTypeStore().makeOrganizationType
                                                        .setId(orgTypeLangVariantReflection(0))
                                                        .setCreatedAt(Try(orgTypeLangVariantReflection(6).asInstanceOf[Timestamp]) match {
                                                            case Success(createdAt) => createdAt
                                                            case _ => null
                                                        })
                                                        .setUpdatedAt(Try(orgTypeLangVariantReflection(7).asInstanceOf[Timestamp]) match {
                                                            case Success(updatedAt) => updatedAt
                                                            case _ => null
                                                        })
                                                        .setLabelTextId(orgTypeLangVariantReflection(5).toString)
                                                        .setLabel(
                                                            Language.apply
                                                            .setId(orgTypeLangVariantReflection(3).toString)
                                                            .setCode(orgTypeLangVariantReflection(2).toString)
                                                            .setLabel(orgTypeLangVariantReflection(4).toString),
                                                            orgTypeLangVariantReflection(1).toString
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            })
                            orgType match {
                                case Some(value) => organization.setType(value)
                                case None => ()
                            }
                            Right(organization)
                        }
                    ) yield organization)
                    .getOrElse(null)
                })
                Future.successful(organizations.toList)
            }
            case Failure(cause) => throw cause
        })
    }

    // Get a single existing organization from database by its id
    def getOrganizationById(id: String)(implicit ec: ExecutionContext): Future[Organization] = {
        getOrganizations(
            OrganizationStore.GetOrganizationsFilters(
                List(
                    OrganizationStore.GetOrganizationsFilter(
                        List(id), List(), List(), None, None, None
                    )
                ),
                List()
            )
        ).transformWith({
            case Success(organizations) => 
                organizations.length match {
                    case 0 => Future.failed(new Error(s"Organization ${id} couldn't be found")) // TODO : changer pour une custom
                    case 1 => Future.successful(organizations(0))
                    case _ => Future.failed(new Error(s"Duplicate id issue in OrganizationStore")) // TODO : changer pour une custom
                }
            case Failure(cause) => Future.failed(new Exception("bla bla bla", cause)) // TODO : changer pour une custom
        })
    }

    // Save organization object's modification to database
    def persistOrganization(organization: Organization)(implicit ec: ExecutionContext): Future[Unit] = {
        Utils.igniteToScalaFuture(igniteCache.putAsync(
            organization.id, organization
        )).transformWith({
            case Success(value) => Future.unit
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
    def bulkPersistOrganizations(organizations: List[Organization])(implicit ec: ExecutionContext): Future[BulkPersistOrganizationsResult] = {
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
    def deleteOrganization(organization: Organization)(implicit ec: ExecutionContext): Future[Unit] = {
        Utils.igniteToScalaFuture(igniteCache.removeAsync(organization.id))
        .transformWith({
            case Success(value) => Future.unit
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
    def bulkDeleteOrganizations(organizations: List[Organization])(implicit ec: ExecutionContext): Future[BulkDeleteOrganizationsResult] = {
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