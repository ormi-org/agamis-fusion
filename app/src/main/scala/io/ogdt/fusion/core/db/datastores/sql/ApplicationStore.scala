package io.ogdt.fusion.core.db.datastores.sql

import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import io.ogdt.fusion.core.db.datastores.typed.sql.SqlStoreQuery
import io.ogdt.fusion.core.db.datastores.typed.SqlMutableStore
import io.ogdt.fusion.core.db.datastores.typed.sql.GetEntityFilters
import java.sql.Timestamp
import org.apache.ignite.IgniteCache
import java.util.UUID
import io.ogdt.fusion.core.db.models.sql.Application
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.cache.CacheAtomicityMode
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.collection.mutable.ListBuffer
import scala.util.Success
import scala.util.Failure

import scala.jdk.CollectionConverters._

import io.ogdt.fusion.core.db.datastores.sql.exceptions.applications.{
    ApplicationNotPersistedException,
    ApplicationNotFoundException,
    DuplicateApplicationException,
    ApplicationQueryExecutionException
}
import io.ogdt.fusion.core.db.common.Utils
import io.ogdt.fusion.core.db.models.sql.generics.exceptions.RelationAlreadyExistsException
import io.ogdt.fusion.core.db.models.sql.relations.OrganizationApplication
import io.ogdt.fusion.core.db.datastores.sql.exceptions.organizations.OrganizationNotFoundException
import java.time.Instant
import scala.util.Try
import io.ogdt.fusion.core.db.datastores.sql.exceptions.organizations.DuplicateOrganizationException
import io.ogdt.fusion.core.db.models.sql.generics.Language
import io.ogdt.fusion.core.db.datastores.sql.exceptions.NoEntryException

class ApplicationStore(implicit wrapper: IgniteClientNodeWrapper) extends SqlMutableStore[UUID, Application] {

    override val schema: String = "FUSION"
    override val cache: String = s"SQL_${schema}_APPLICATION"
    override protected var igniteCache: IgniteCache[UUID, Application] = wrapper.cacheExists(cache) match {
        case true => wrapper.getCache[UUID, Application](cache)
        case false => {
            wrapper.createCache[UUID, Application](
                wrapper.makeCacheConfig[UUID, Application]
                .setCacheMode(CacheMode.REPLICATED)
                .setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL)
                .setDataRegionName("Fusion")
                .setName(cache)
                .setSqlSchema(schema)
                .setIndexedTypes(classOf[UUID], classOf[Application])
            )
        }
    }

    // Create Application object 
    def makeApplication: Application = {
        implicit val ApplicationStore: ApplicationStore = this
        new Application
    }

    def makeApplicationQuery(queryFilters: ApplicationStore.GetApplicationsFilters): SqlStoreQuery = {
        var queryString: String = 
            "SELECT app_id, app_label, app_version, app_universal_id, app_status, app_manifest_url, app_store_url, app_created_at, app_updated_at, info_data, type_data" +
            "FROM " +
            "((SELECT app_id, app_label, app_version, app_universal_id, app_status, app_manifest_url, app_store_url, app_created_at, app_updated_at, info_data, type_data" +
            "FROM " +
            "(SELECT APP.id AS app_id, APP.label AS app_label, APP.version AS app_version, APP.app_universal_id AS app_universal_id, APP.status AS app_status, APP.manifest_url AS app_manifest_url, APP.store_url AS app_store_url, APP.created_at AS app_created_at, APP.updated_at AS app_updated_at," +
            "CONCAT_WS('||', ORG.id, ORG.label, ORG.queryable, ORG_APP.status, ORG_APP.license_file_fs_id, ORG_APP.license_file_id, ORG.created_at, ORG.updated_at) AS info_data, 'ORGANIZATION' AS type_data, ORG.id AS org_id" +
            "FROM FUSION.APPLICATION AS APP" +
            "RIGHT OUTER JOIN FUSION.ORGANIZATION_APPLICATION AS ORG_APP ON APP.id = ORG_APP.application_id" +
            "LEFT OUTER JOIN FUSION.ORGANIZATION AS ORG ON ORG_APP.organization_id = ORG.id" +
            "UNION ALL" +
            "(SELECT app_id, app_label, app_version, app_universal_id, app_status, app_manifest_url, app_store_url, app_created_at, app_updated_at, info_data, type_data, org_id" +
            "FROM " +
            "(SELECT APP.id AS app_id, APP.label AS app_label, APP.version AS app_version, APP.app_universal_id AS app_universal_id, APP.status AS app_status, APP.manifest_url AS app_manifest_url, APP.store_url AS app_store_url, APP.created_at AS app_created_at, APP.updated_at AS app_updated_at," +
            "CONCAT_WS('||', ORG.id, ORGTYPE.id, ORGTYPE.label_text_id, ORGTYPE.created_at, ORGTYPE.updated_at) AS info_data, 'ORGTYPE' AS type_data, ORGTYPE.id AS orgtype_id, ORG.id AS org_id" +
            "FROM FUSION.APPLICATION AS APP" +
            "RIGHT OUTER JOIN FUSION.ORGANIZATION_APPLICATION AS ORG_APP ON APP.id = ORG_APP.application_id" +
            "LEFT OUTER JOIN FUSION.ORGANIZATION AS ORG ON ORG_APP.organization_id = ORG.id" +
            "LEFT OUTER JOIN FUSION.ORGANIZATIONTYPE AS ORGTYPE ON ORG.organizationtype_id = ORGTYPE.id" +
            "UNION ALL" +
            "(SELECT APP.id AS app_id, APP.label AS app_label, APP.version AS app_version, APP.app_universal_id AS app_universal_id, APP.status AS app_status, APP.manifest_url AS app_manifest_url, APP.store_url AS app_store_url, APP.created_at AS app_created_at, APP.updated_at AS app_updated_at," +
            "CONCAT_WS('||', ORG.id, ORGTYPE.id, TEXT.content, LANG.code, TEXT.language_id, LANG.label) AS info_data, 'ORGTYPE_LANG_VARIANT' AS type_data, ORGTYPE.id AS orgtype_id, ORG.id AS org_id" +
            "FROM FUSION.APPLICATION AS APP" +
            "RIGHT OUTER JOIN FUSION.ORGANIZATION_APPLICATION AS ORG_APP ON APP.id = ORG_APP.application_id" +
            "LEFT OUTER JOIN FUSION.ORGANIZATION AS ORG ON ORG_APP.organization_id = ORG.id" +
            "LEFT OUTER JOIN FUSION.ORGANIZATIONTYPE AS ORGTYPE ON ORG.organizationtype_id = ORGTYPE.id" +
            "LEFT OUTER JOIN FUSION.TEXT AS TEXT ON TEXT.id = ORGTYPE.label_text_id" +
            "LEFT OUTER JOIN FUSION.LANGUAGE AS LANG ON TEXT.language_id = LANG.id))" +
            "ORDER BY orgtype_id))" +
            "ORDER BY app_id, org_id)" +
            "UNION ALL" +
            "(SELECT app_id, app_label, app_version, app_universal_id, app_status, app_manifest_url, app_store_url, app_created_at, app_updated_at, info_data, type_data" +
            "FROM" +
            "(SELECT APP.id AS app_id, APP.label AS app_label, APP.version AS app_version, APP.app_universal_id AS app_universal_id, APP.status AS app_status, APP.manifest_url AS app_manifest_url, APP.store_url AS app_store_url, APP.created_at AS app_created_at, APP.updated_at AS app_updated_at," +
            "CONCAT_WS('||', PERMISSION.id, PERMISSION.key, PERMISSION.label_text_id, PERMISSION.description_text_id, PERMISSION.editable, PERMISSION.application_id, PERMISSION.created_at, PERMISSION.updated_at) AS info_data, 'PERMISSION' AS type_data, PERMISSION.id AS permission_id" +
            "FROM FUSION.APPLICATION AS APP" +
            "INNER JOIN FUSION.PERMISSION AS PERMISSION ON APP.id = PERMISSION.application_id" +
            "UNION ALL" +
            "(SELECT app_id, app_label, app_version, app_universal_id, app_status, app_manifest_url, app_store_url, app_created_at, app_updated_at," +
            "CONCAT_WS('||', permission_id, GROUP_CONCAT(content SEPARATOR ';'), code, language_id, label, GROUP_CONCAT(text_id SEPARATOR ';')) AS info_data, 'PERMISSION_LANG_VARIANT' AS type_data, permission_id" +
            "FROM " +
            "(SELECT APP.id AS app_id, APP.label AS app_label, PERMISSION.id AS permission_id, TEXT.content, LANG.code, TEXT.language_id, LANG.label, TEXT.id AS text_id, APP.version AS app_version, APP.app_universal_id AS app_universal_id, APP.status AS app_status, APP.manifest_url AS app_manifest_url, APP.store_url AS app_store_url, APP.created_at AS app_created_at, APP.updated_at AS app_updated_at" +
            "FROM FUSION.APPLICATION AS APP" +
            "RIGHT OUTER JOIN FUSION.PERMISSION AS PERMISSION ON APP.id = PERMISSION.application_id" +
            "LEFT OUTER JOIN FUSION.TEXT AS TEXT ON TEXT.id = PERMISSION.label_text_id" +
            "LEFT OUTER JOIN FUSION.LANGUAGE AS LANG ON TEXT.language_id = LANG.id" +
            "UNION ALL" +
            "SELECT APP.id AS app_id, APP.label AS app_label, PERMISSION.id AS permission_id, TEXT.content, LANG.code, TEXT.language_id, LANG.label, TEXT.id AS text_id, APP.version AS app_version, APP.app_universal_id AS app_universal_id, APP.status AS app_status, APP.manifest_url AS app_manifest_url, APP.store_url AS app_store_url, APP.created_at AS app_created_at, APP.updated_at AS app_updated_at" +
            "FROM FUSION.APPLICATION AS APP" +
            "RIGHT OUTER JOIN FUSION.PERMISSION AS PERMISSION ON APP.id = PERMISSION.application_id" +
            "LEFT OUTER JOIN FUSION.TEXT AS TEXT ON TEXT.id = PERMISSION.description_text_id" +
            "LEFT OUTER JOIN FUSION.LANGUAGE AS LANG ON TEXT.language_id = LANG.id)" +
            "GROUP BY permission_id, language_id))" +
            "ORDER BY app_id, permission_id))"
        var queryArgs: ListBuffer[String] = ListBuffer()
        var whereStatements: ListBuffer[String] = ListBuffer()
        queryFilters.filters.foreach({ filter =>
            var innerWhereStatement: ListBuffer[String] = ListBuffer()
            // manage ids search
            if (!filter.id.isEmpty) {
                innerWhereStatement += s"app_id in (${(for (i <- 1 to filter.id.length) yield "?").mkString(",")})"
                queryArgs ++= filter.id
            }
            // manage appUniversalId search
            if (!filter.appUniversalId.isEmpty) {
                innerWhereStatement += s"app_universal_id in (${(for (i <- 1 to filter.id.length) yield "?").mkString(",")})"
                queryArgs ++= filter.appUniversalId
            }
            // manage status search
            if (!filter.status.isEmpty) {
                innerWhereStatement += s"app_status in (${(for (i <- 1 to filter.id.length) yield "?").mkString(",")})"
                queryArgs ++= filter.status.map({ status =>
                    status.toInt.toString
                })
            }
            // manage manifestUrl search
            if (!filter.manifestUrl.isEmpty) {
                innerWhereStatement += s"app_manifest_url in (${(for (i <- 1 to filter.id.length) yield "?").mkString(",")})"
                queryArgs ++= filter.manifestUrl
            }
            // manage storeUrl search
            if (!filter.storeUrl.isEmpty) {
                innerWhereStatement += s"app_store_url in (${(for (i <- 1 to filter.id.length) yield "?").mkString(",")})"
                queryArgs ++= filter.storeUrl
            }
            // manage metadate search
            filter.createdAt match {
                case Some((test, time)) => {
                    innerWhereStatement += s"app_created_at ${
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
                    innerWhereStatement += s"app_updated_at ${
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
                s"app_${o._1} ${o._2 match {
                    case 1 => "ASC"
                    case -1 => "DESC"
                }}"
            ).mkString(", ")}"
        }
        makeQuery(queryString)
        .setParams(queryArgs.toList)
    }

    // Get existing applications from database
    def getApplications(queryFilters: ApplicationStore.GetApplicationsFilters)(implicit ec: ExecutionContext): Future[List[Application]] = {
        executeQuery(makeApplicationQuery(queryFilters)).transformWith({
            case Success(applicationResults) => {
                val applications = applicationResults.toList.groupBy(_(0)).map(entityReflection => {
                    (for (
                        application <- Right(makeApplication
                            .setId(entityReflection._2(0)(0).toString)
                            .setLabel(entityReflection._2(0)(1).toString)
                            .setVersion(entityReflection._2(0)(2).toString)
                            .setAppUniversalId(entityReflection._2(0)(3).toString)
                            .setManifestUrl(entityReflection._2(0)(5).toString)
                            .setStoreUrl(entityReflection._2(0)(6).toString)
                            .setCreatedAt(entityReflection._2(0)(7) match {
                                case createdAt: Timestamp => createdAt
                                case _ => null
                            })
                            .setUpdatedAt(entityReflection._2(0)(8) match {
                                case updatedAt: Timestamp => updatedAt
                                case _ => null
                            })
                        ) flatMap { application =>
                            Application.Status.fromInt(entityReflection._2(0)(4).asInstanceOf[Int]) match {
                                case Success(status) => {
                                    Right(application.setStatus(status))
                                }
                                case Failure(cause) => Right(application)
                            }
                        } flatMap { application =>
                            val groupedRows = getRelationsGroupedRowsFrom(entityReflection._2, 9, 10)

                            groupedRows.get("ORGANIZATION") match {
                                case Some(organizationReflections) => {
                                    organizationReflections.foreach({ organizationReflection =>
                                        organizationReflection.partition(_._1 == "ORGANIZATION") match {
                                            case result => {
                                                result._1.length match {
                                                    case 0 => Future.failed(OrganizationNotFoundException(s"Application ${application.id}:${application.label} might be orphan"))
                                                    case 1 => {
                                                        val orgDef = result._1(0)._2
                                                        (for (
                                                            organization <- Right(new OrganizationStore()
                                                                .makeOrganization
                                                                .setId(orgDef(0))
                                                                .setLabel(orgDef(1))
                                                                .setCreatedAt(Timestamp.from(Instant.parse(orgDef(6))) match {
                                                                        case createdAt: Timestamp => createdAt
                                                                        case _ => null
                                                                    })
                                                                    .setUpdatedAt(Timestamp.from(Instant.parse(orgDef(7))) match {
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
                                                                                    ) flatMap { orgType =>
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
                                                                                        Right(orgType)
                                                                                    }
                                                                                ) yield organization.setType(orgType))
                                                                            }
                                                                            case _ =>
                                                                        }
                                                                    }
                                                                }
                                                                Right(organization)
                                                            };
                                                            licenseStore = orgDef(4).toString;
                                                            licenseFileId = orgDef(5).toString;
                                                            orgAppStatus <- Right {
                                                                OrganizationApplication.Status.fromInt(orgDef(3).asInstanceOf[Int]) match {
                                                                    case Success(status) => status
                                                                    case Failure(cause) => throw cause
                                                                }
                                                            }
                                                        ) yield application.addOrganization(
                                                            organization,
                                                            new FileSystemStore()
                                                                .makeFileSystem
                                                                .setId(licenseStore),
                                                            licenseFileId,
                                                            orgAppStatus
                                                        ))
                                                    }
                                                    case _ => Future.failed(DuplicateOrganizationException(s"Application ${application.id}:${application.label} has duplicate organization relation"))
                                                }
                                            }
                                        }
                                    })
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
                                                                .setRelatedApplication(application)
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
                                                        ) yield application.addPermission(permission))
                                                    }
                                                }
                                            }
                                        }
                                    })
                                }
                                case None => {}
                            }
                            Right(application)
                        }
                    ) yield application).getOrElse(null)
                })
                Future.successful(applications.toList)
            }
            case Failure(cause) => Future.failed(ApplicationQueryExecutionException(cause))
        })
    }

    def getAllApplications(implicit ec: ExecutionContext): Future[List[Application]] = {
        getApplications(ApplicationStore.GetApplicationsFilters().copy(
            orderBy = List(
                ("id", 1)
            )
        )).transformWith({
            case Success(applications) => 
                applications.length match {
                    case 0 => Future.failed(new NoEntryException("Application store is empty"))
                    case _ => Future.successful(applications)
                }
            case Failure(cause) => Future.failed(cause)
        })
    }

    def getApplicationById(id: String)(implicit ec: ExecutionContext): Future[Application] = {
        getApplications(
            ApplicationStore.GetApplicationsFilters().copy(
                filters = List(
                    ApplicationStore.GetApplicationsFilter().copy(
                        id = List(id)
                    )
                )
            )
        ).transformWith({
            case Success(applications) => 
                applications.length match {
                    case 0 => Future.failed(new ApplicationNotFoundException(s"Application ${id} couldn't be found"))
                    case 1 => Future.successful(applications(0))
                    case _ => Future.failed(new DuplicateApplicationException)
                }
            case Failure(cause) => Future.failed(cause)
        })
    }

    def persistApplication(application: Application)(implicit ec: ExecutionContext): Future[Unit] = {
        val transaction = makeTransaction
        transaction match {
            case Success(tx) => {
                val organizationStore = new OrganizationStore()
                Future.sequence(
                    List(
                        Utils.igniteToScalaFuture(igniteCache.putAsync(
                            application.id, application
                        )),
                        Future.sequence(
                            List(
                                organizationStore
                                    .getOrganizations(
                                        OrganizationStore.GetOrganizationsFilters().copy(
                                            filters = List(
                                                OrganizationStore.GetOrganizationsFilter()
                                                .copy(id = application.organizations.filter(_._1 == true).map(_._2._2.id.toString))
                                            ),
                                            orderBy = List(
                                                ("id", 1)
                                            )
                                        )
                                    ).transformWith({
                                        case Success(organizations) => {
                                            val orgs = organizations zip application.organizations.sortBy(_._2._2.id).map(_._2._1)
                                            Future.successful(orgs.flatMap({ o =>
                                                try {
                                                    Some(o._1.addApplication(application, o._2))
                                                } catch {
                                                    case e: RelationAlreadyExistsException => None
                                                }
                                            }))
                                        }
                                        case Failure(cause) => Future.failed(cause)
                                    }),
                                organizationStore
                                    .getOrganizations(
                                        OrganizationStore.GetOrganizationsFilters().copy(
                                            filters = List(
                                                OrganizationStore.GetOrganizationsFilter()
                                                .copy(id = application.organizations.filter(_._1 == false).map(_._2._2.id.toString))
                                            )
                                        )
                                    ).transformWith({
                                        case Success(organizations) => {
                                            Future.successful(organizations.map({ o =>
                                                o.removeApplication(application)
                                            }))
                                        }
                                        case Failure(cause) => Future.failed(cause)
                                    })
                            )
                        ).transformWith({
                            case Success(organizations) => {                            
                                organizationStore.bulkPersistOrganizations(organizations.flatten)
                            }
                            case Failure(cause) => Future.failed(cause)
                        })
                    )
                ).transformWith({
                    case Success(value) => {
                        commitTransaction(transaction).transformWith({
                            case Success(value) => Future.unit
                            case Failure(cause) => Future.failed(ApplicationNotPersistedException(cause))
                        })
                    }
                    case Failure(cause) => {
                        rollbackTransaction(transaction)
                        Future.failed(ApplicationNotPersistedException(cause))
                    }
                })
            }
            case Failure(cause) => Future.failed(ApplicationNotPersistedException(cause))
        }
    }

    def bulkPersistApplications(applications: List[Application])(implicit ec: ExecutionContext): Future[Unit] = {
        val transaction = makeTransaction
        transaction match {
            case Success(tx) => {
                val organizationStore = new OrganizationStore()
                Future.sequence(
                    List(
                        Utils.igniteToScalaFuture(igniteCache.putAllAsync(
                            (applications.map(_.id) zip applications).toMap[UUID, Application].asJava
                        )),
                        Future.sequence(
                            applications.map({ application =>
                                List(
                                    organizationStore
                                    .getOrganizations(
                                        OrganizationStore.GetOrganizationsFilters().copy(
                                            filters = List(
                                                OrganizationStore.GetOrganizationsFilter()
                                                .copy(id = application.organizations.filter(_._1 == true).map(_._2._2.id.toString))
                                            ),
                                            orderBy = List(
                                                ("id", 1)
                                            )
                                        )
                                    ).transformWith({
                                        case Success(organizations) => {
                                            val orgs = organizations zip application.organizations.sortBy(_._2._2.id).map(_._2._1)
                                            Future.successful(orgs.flatMap({ o =>
                                                try {
                                                    Some(o._1.addApplication(application, o._2))
                                                } catch {
                                                    case e: RelationAlreadyExistsException => None
                                                }
                                            }))
                                        }
                                        case Failure(cause) => Future.failed(cause)
                                    }),
                                    organizationStore
                                    .getOrganizations(
                                        OrganizationStore.GetOrganizationsFilters().copy(
                                            filters = List(
                                                OrganizationStore.GetOrganizationsFilter()
                                                .copy(id = application.organizations.filter(_._1 == false).map(_._2._2.id.toString))
                                            )
                                        )
                                    ).transformWith({
                                        case Success(organizations) => {
                                            Future.successful(organizations.map({ o =>
                                                o.removeApplication(application)
                                            }))
                                        }
                                        case Failure(cause) => Future.failed(cause)
                                    })
                                )
                            }).flatten
                        ).transformWith({
                            case Success(organizations) => {
                                organizationStore.bulkPersistOrganizations(organizations.flatten)
                            }
                            case Failure(cause) => Future.failed(cause)
                        })
                    )
                ).transformWith({
                    case Success(value) => {
                        commitTransaction(transaction).transformWith({
                            case Success(value) => Future.unit
                            case Failure(cause) => Future.failed(ApplicationNotPersistedException(cause))
                        })
                    }
                    case Failure(cause) => {
                        rollbackTransaction(transaction)
                        Future.failed(ApplicationNotPersistedException(cause))
                    }
                })
            }
            case Failure(cause) => Future.failed(ApplicationNotPersistedException(cause))
        }
    }

    def deleteApplication(application: Application)(implicit ec: ExecutionContext): Future[Unit] = {
        val transaction = makeTransaction
        transaction match {
            case Success(tx) => {
                val organizationStore = new OrganizationStore()
                Future.sequence(
                    List(
                        Utils.igniteToScalaFuture(igniteCache.removeAsync(application.id)),
                        organizationStore
                            .getOrganizations(
                                OrganizationStore.GetOrganizationsFilters().copy(filters = List(
                                    OrganizationStore.GetOrganizationsFilter()
                                    .copy(id = application.organizations.map(_._2._2.id.toString))
                                ))
                            ).transformWith({
                                case Success(organizations) => {
                                    organizationStore.bulkPersistOrganizations(
                                        organizations.map(_.removeApplication(application))
                                    )
                                }
                                case Failure(cause) => Future.failed(cause)
                            })
                    )
                ).transformWith({
                    case Success(value) => {
                        commitTransaction(transaction).transformWith({
                            case Success(value) => Future.unit
                            case Failure(cause) => Future.failed(ApplicationNotPersistedException(cause))
                        })
                    }
                    case Failure(cause) => {
                        rollbackTransaction(transaction)
                        Future.failed(ApplicationNotPersistedException(cause))
                    }
                })
            }
            case Failure(cause) => Future.failed(ApplicationNotPersistedException(cause))
        }
        Future.unit
    }

}

object ApplicationStore {
    case class GetApplicationsFilter(
        id: List[String] = List(),
        appUniversalId: List[String] = List(),
        status: List[Application.Status] = List(),
        manifestUrl: List[String] = List(),
        storeUrl: List[String] = List(),
        createdAt: Option[(String, Timestamp)] = None,
        updatedAt: Option[(String, Timestamp)] = None
    )
    case class GetApplicationsFilters(
        filters: List[GetApplicationsFilter] = List(),
        orderBy: List[(String,Int)] = List()
    ) extends GetEntityFilters
}