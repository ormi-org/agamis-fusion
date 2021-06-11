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
import io.ogdt.fusion.core.db.models.sql.relations.OrganizationApplication

import io.ogdt.fusion.core.db.datastores.sql.exceptions.organizations.{
    OrganizationNotPersistedException,
    OrganizationQueryExecutionException,
    DuplicateOrganizationException,
    OrganizationNotFoundException
}

import io.ogdt.fusion.core.db.datastores.sql.exceptions.organizationtypes.{
    OrganizationtypeNotFoundException,
    DuplicateOrganizationtypeException
}

import org.apache.ignite.cache.QueryEntity
import java.time.Instant
import io.ogdt.fusion.core.db.models.sql.generics.Email
import io.ogdt.fusion.core.db.datastores.sql.exceptions.NoEntryException

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
                .setQueryEntities(
                    List(
                        new QueryEntity(classOf[String], classOf[OrganizationApplication])
                        .setTableName("ORGANIZATION_APPLICATION")
                    ).asJava
                )
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
            "CONCAT_WS('||',	PROFILE.id, lastname, firstname, EMAIL.address, last_login, is_active, user_id, PROFILE.organization_id , PROFILE.created_at , PROFILE.updated_at) AS info_data, 'PROFILE' AS type_data  " +
            s"FROM $schema.ORGANIZATION as ORG " +
            s"INNER JOIN $schema.PROFILE AS PROFILE ON PROFILE.organization_id = ORG.id " +
            s"LEFT OUTER JOIN $schema.PROFILE_EMAIL AS PROFILE_EMAIL ON PROFILE_EMAIL.profile_id = PROFILE.id " +
            s"LEFT OUTER JOIN $schema.EMAIL AS EMAIL ON PROFILE_EMAIL.email_id = EMAIL.id " +
            "WHERE PROFILE_EMAIL.is_main = TRUE " +
            "UNION ALL " +
            "SELECT ORG.id AS org_id, ORG.label AS org_label, queryable AS org_queryable, ORG.created_at AS org_created_at, ORG.updated_at AS org_updated_at, " +
            "CONCAT_WS('||',FS.id, rootdir_id, FS.label, shared, FS_ORG.is_default, FS.created_at, FS.updated_at) AS info_data, 'FS' AS type_data " +
            s"FROM $schema.ORGANIZATION as ORG " +
            s"LEFT OUTER JOIN $schema.FILESYSTEM_ORGANIZATION AS FS_ORG ON FS_ORG.organization_id = ORG.id " +
            s"LEFT OUTER JOIN $schema.FILESYSTEM AS FS ON FS_ORG.filesystem_id = FS.id " +
            "UNION ALL " +
            "(SELECT org_id, org_label, org_queryable, org_created_at, org_updated_at, info_data, type_data " +
            "FROM " +
            "(SELECT ORG.id AS org_id, ORG.label AS org_label, queryable AS org_queryable, ORG.created_at AS org_created_at, ORG.updated_at AS org_updated_at, " +
            "CONCAT_WS('||', ORG.id, ORGTYPE.id, ORGTYPE.label_text_id, ORGTYPE.created_at, ORGTYPE.updated_at) AS info_data, 'ORGTYPE' AS type_data, ORGTYPE.id AS orgtype_id " +
            s"FROM $schema.ORGANIZATION AS ORG " +
            s"LEFT OUTER JOIN $schema.ORGANIZATIONTYPE AS ORGTYPE ON ORG.organizationtype_id = ORGTYPE.id " +
            "UNION ALL " +
            "(SELECT ORG.id AS org_id, ORG.label AS org_label, queryable AS org_queryable, ORG.created_at AS org_created_at, ORG.updated_at AS org_updated_at, " +
            "CONCAT_WS('||', ORG.id, ORGTYPE.id, TEXT.content, LANG.code, TEXT.language_id, LANG.label) AS info_data, 'ORGTYPE_LANG_VARIANT' AS type_data, ORGTYPE.id AS orgtype_id " +
            s"FROM $schema.ORGANIZATION AS ORG " +
            s"LEFT OUTER JOIN $schema.ORGANIZATIONTYPE AS ORGTYPE ON ORG.organizationtype_id = ORGTYPE.id " +
            s"LEFT OUTER JOIN $schema.TEXT AS TEXT ON TEXT.id = ORGTYPE.label_text_id " +
            s"LEFT OUTER JOIN $schema.LANGUAGE AS LANG ON TEXT.language_id = LANG.id)) " +
            "ORDER BY org_id, orgtype_id))"
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
                            val groupedRows = getRelationsGroupedRowsFrom(entityReflection._2, 5, 6)
                            
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
                                                .setRelatedUser(new UserStore()
                                                    .makeUser
                                                    .setId(profileDef(6))
                                                )
                                                .setRelatedOrganization(new OrganizationStore()
                                                    .makeOrganization
                                                    .setId(profileDef(7))
                                                )
                                                .setCreatedAt(Timestamp.from(Instant.parse(profileDef(8))) match {
                                                    case createdAt: Timestamp => createdAt
                                                    case _ => null
                                                })
                                                .setUpdatedAt(Timestamp.from(Instant.parse(profileDef(9))) match {
                                                    case updatedAt: Timestamp => updatedAt
                                                    case _ => null
                                                })
                                            ) flatMap { profile =>
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
                                        ) yield organization.addRelatedProfile(profile))
                                    })
                                }
                                case None => {}
                            }

                            groupedRows.get("FS") match {
                                case Some(fsReflections) => {
                                    fsReflections.foreach({ fsReflection =>
                                        val fsDef = fsReflection(0)._2
                                        (for (
                                            fileSystem <- Right(new FileSystemStore()
                                                .makeFileSystem
                                                .setId(fsDef(0))
                                                .setRootdirId(fsDef(1))
                                                .setLabel(fsDef(2))
                                                .setCreatedAt(Try(fsDef(5).asInstanceOf[Timestamp]) match {
                                                    case Success(createdAt) => createdAt
                                                    case _ => null
                                                })
                                                .setUpdatedAt(Try(fsDef(6).asInstanceOf[Timestamp]) match {
                                                    case Success(updatedAt) => updatedAt
                                                    case _ => null
                                                })
                                            ) flatMap { fileSystem =>
                                                try {
                                                    if (fsDef(3).toBoolean) Right(fileSystem.setShared)
                                                    else Right(fileSystem.setUnshared)
                                                } catch {
                                                    case e: IllegalArgumentException => Right(fileSystem)
                                                }
                                            }
                                        ) yield {
                                            try {
                                                if (fsDef(4).toBoolean) Right(organization.setDefaultFileSystem(fileSystem))
                                                else Right(organization.addFileSystem(fileSystem))
                                            } catch {
                                                case e: IllegalArgumentException => Right(organization)
                                            }
                                        })
                                    })
                                }
                                case None => {}
                            }

                            groupedRows.get("ORGTYPE") match {
                                case Some(orgTypeReflections) => {
                                    orgTypeReflections.foreach({ orgTypeReflection =>
                                        orgTypeReflection.partition(_._1 == "ORGTYPE") match {
                                            case result => {
                                                result._1.length match {
                                                    case 0 => Future.failed(OrganizationtypeNotFoundException(s"Organization type of organization ${organization.id}:${organization.label} might be undefined"))
                                                    case 1 => {
                                                        val orgTypeDef = result._1(0)._2
                                                        (for (
                                                            orgType <- Right(new OrganizationTypeStore()
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
                                                            )
                                                        ) yield organization.setType(orgType) )
                                                    }
                                                    case _ => Future.failed(DuplicateOrganizationtypeException(s"Organization ${organization.id}:${organization.label} has duplicate organization types"))
                                                }
                                            }
                                        }
                                    })
                                }
                                case None => {}
                            }
                            Right(organization)
                        }
                    ) yield organization)
                    .getOrElse(null)
                })
                Future.successful(organizations.toList)
            }
            case Failure(cause) => Future.failed(OrganizationNotPersistedException(cause))
        })
    }

    def getAllOrganizations(implicit ec: ExecutionContext): Future[List[Organization]] = {
        getOrganizations(OrganizationStore.GetOrganizationsFilters().copy(
            orderBy = List(
                ("id", 1)
            )
        )).transformWith({
            case Success(organizations) => 
                organizations.length match {
                    case 0 => Future.failed(new NoEntryException("Organization store is empty"))
                    case _ => Future.successful(organizations)
                }
            case Failure(cause) => Future.failed(cause)
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
                    case 0 => Future.failed(new OrganizationNotFoundException(s"Organization ${id} couldn't be found"))
                    case 1 => Future.successful(organizations(0))
                    case _ => Future.failed(new DuplicateOrganizationException)
                }
            case Failure(cause) => Future.failed(cause)
        })
    }

    // Save organization object's modification to database
    def persistOrganization(organization: Organization)(implicit ec: ExecutionContext): Future[Unit] = {
        Utils.igniteToScalaFuture(igniteCache.putAsync(
            organization.id, organization
        )).transformWith({
            case Success(value) => Future.unit
            case Failure(cause) => Future.failed(OrganizationNotPersistedException(cause))
        })
    }

    // Save several object's modifications
    def bulkPersistOrganizations(organizations: List[Organization])(implicit ec: ExecutionContext): Future[Unit] = {
        Utils.igniteToScalaFuture(igniteCache.putAllAsync(
            (organizations.map(_.id) zip organizations).toMap[UUID, Organization].asJava
        )).transformWith({
            case Success(value) => Future.unit
            case Failure(cause) => Future.failed(OrganizationNotPersistedException(cause))
        })
    }

    // Delete organization from database
    def deleteOrganization(organization: Organization)(implicit ec: ExecutionContext): Future[Unit] = {
        if (!organization.relatedProfiles.isEmpty) return Future.failed(OrganizationNotPersistedException("Organization still contains profiles and can't be deleted"))
        Utils.igniteToScalaFuture(igniteCache.removeAsync(organization.id))
        .transformWith({
            case Success(value) => Future.unit
            case Failure(cause) => Future.failed(OrganizationNotPersistedException(cause))
        })
    }

    // Delete several organizations from database
    def bulkDeleteOrganizations(organizations: List[Organization])(implicit ec: ExecutionContext): Future[Unit] = {
        organizations.find({ org => !org.relatedProfiles.isEmpty }) match {
            case Some(found) => Future.failed(OrganizationNotPersistedException(s"Organization ${found.id}:${found.label} still contains profiles and can't be deleted"))
            case None => {
                Utils.igniteToScalaFuture(igniteCache.removeAllAsync(organizations.map(_.id).toSet.asJava))
                .transformWith({
                    case Success(value) => Future.unit
                    case Failure(cause) => Future.failed(OrganizationNotPersistedException(cause))
                })
            }
        }
    }
}

object OrganizationStore {
    case class GetOrganizationsFilter(
        id: List[String] = List(),
        label: List[String] = List(),
        `type`: List[String] = List(),
        queryable: Option[Boolean] = None,
        createdAt: Option[(String, Timestamp)] = None, // (date, (eq, lt, gt, ne))
        updatedAt: Option[(String, Timestamp)] = None // (date, (eq, lt, gt, ne))
    )
    case class GetOrganizationsFilters(
        filters: List[GetOrganizationsFilter] = List(),
        orderBy: List[(String, Int)] = List()
    ) extends GetEntityFilters
}