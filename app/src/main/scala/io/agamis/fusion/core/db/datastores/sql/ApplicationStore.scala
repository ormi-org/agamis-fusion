package io.agamis.fusion.core.db.datastores.sql

import io.agamis.fusion.core.db.common.Utils
import io.agamis.fusion.core.db.datastores.sql.exceptions.NoEntryException
import io.agamis.fusion.core.db.datastores.sql.exceptions.typed.applications.{ApplicationNotFoundException, ApplicationNotPersistedException, ApplicationQueryExecutionException, DuplicateApplicationException}
import io.agamis.fusion.core.db.datastores.sql.exceptions.typed.organizations.{DuplicateOrganizationException, OrganizationNotFoundException}
import io.agamis.fusion.core.db.datastores.sql.generics.exceptions.texts.TextNotFoundException
import io.agamis.fusion.core.db.datastores.typed.SqlMutableStore
import io.agamis.fusion.core.db.datastores.typed.sql.{GetEntityFilters, SqlStoreQuery}
import io.agamis.fusion.core.db.models.sql.Application
import io.agamis.fusion.core.db.models.sql.generics.Language
import io.agamis.fusion.core.db.models.sql.generics.exceptions.RelationAlreadyExistsException
import io.agamis.fusion.core.db.models.sql.relations.OrganizationApplication
import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.{CacheAtomicityMode, CacheMode}

import java.sql.Timestamp
import java.util.UUID
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

class ApplicationStore(implicit wrapper: IgniteClientNodeWrapper) extends SqlMutableStore[UUID, Application] {

  override val schema: String = "FUSION"
  override val cache: String = s"SQL_${schema}_APPLICATION"
  override protected val igniteCache: IgniteCache[UUID, Application] = if (wrapper.cacheExists(cache)) {
    wrapper.getCache[UUID, Application](cache)
  } else {
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

  // Create Application object
  def makeApplication: Application = {
    implicit val ApplicationStore: ApplicationStore = this
    new Application
  }

  def makeApplicationQuery(queryFilters: ApplicationStore.GetApplicationsFilters): SqlStoreQuery = {
    var baseQueryString = queryString.replace("$schema", schema)
    val queryArgs: ListBuffer[String] = ListBuffer()
    val whereStatements: ListBuffer[String] = ListBuffer()
    queryFilters.filters.foreach({ filter =>
      val innerWhereStatement: ListBuffer[String] = ListBuffer()
      // manage ids search
      if (filter.id.nonEmpty) {
        innerWhereStatement += s"app_id in (${(for (_ <- 1 to filter.id.length) yield "?").mkString(",")})"
        queryArgs ++= filter.id
      }
      // manage appUniversalId search
      if (filter.appUniversalId.nonEmpty) {
        innerWhereStatement += s"app_universal_id in (${(for (_ <- 1 to filter.id.length) yield "?").mkString(",")})"
        queryArgs ++= filter.appUniversalId
      }
      // manage status search
      if (filter.status.nonEmpty) {
        innerWhereStatement += s"app_status in (${(for (_ <- 1 to filter.id.length) yield "?").mkString(",")})"
        queryArgs ++= filter.status.map({ status =>
          status.toInt.toString
        })
      }
      // manage manifestUrl search
      if (filter.manifestUrl.nonEmpty) {
        innerWhereStatement += s"app_manifest_url in (${(for (_ <- 1 to filter.id.length) yield "?").mkString(",")})"
        queryArgs ++= filter.manifestUrl
      }
      // manage storeUrl search
      if (filter.storeUrl.nonEmpty) {
        innerWhereStatement += s"app_store_url in (${(for (_ <- 1 to filter.id.length) yield "?").mkString(",")})"
        queryArgs ++= filter.storeUrl
      }
      // manage metadate search
      filter.createdAt match {
        case Some((test, time)) =>
          innerWhereStatement += s"app_created_at ${
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
          innerWhereStatement += s"app_updated_at ${
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
          s"app_${o._1} ${
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

  // Get existing applications from database
  def getApplications(queryFilters: ApplicationStore.GetApplicationsFilters)(implicit ec: ExecutionContext): Future[List[Application]] = {
    executeQuery(makeApplicationQuery(queryFilters)).transformWith({
      case Success(rows) =>
        val entityReflections = rows.groupBy(_.head)
        val applications = rows.map(_.head).distinct.map(entityReflections(_)).map(entityReflection => {
          val groupedRows = getRelationsGroupedRowsFrom(entityReflection, 9, 10)
          groupedRows.get("APPLICATION") match {
            case Some(applicationReflections) =>
              val appDef = applicationReflections.head.head._2
              (for (
                application <- Right(makeApplication
                  .setId(appDef(0))
                  .setLabel(appDef(1))
                  .setVersion(appDef(2))
                  .setAppUniversalId(appDef(3))
                  .setManifestUrl(appDef(5))
                  .setStoreUrl(appDef(6))
                  .setCreatedAt(Utils.timestampFromString(appDef(7)) match {
                    case createdAt: Timestamp => createdAt
                    case _ => null
                  })
                  .setUpdatedAt(Utils.timestampFromString(appDef(8)) match {
                    case updatedAt: Timestamp => updatedAt
                    case _ => null
                  })
                ) flatMap { application =>
                  Application.Status.fromInt(appDef(4).toInt) match {
                    case Success(status) =>
                      Right(application.setStatus(status))
                    case Failure(_) => Right(application)
                  }
                } flatMap { application =>
                  groupedRows.get("ORGANIZATION") match {
                    case Some(organizationReflections) =>
                      organizationReflections.foreach({ organizationReflection =>
                        organizationReflection.partition(_._1 == "ORGANIZATION") match {
                          case result =>
                            result._1.length match {
                              case 0 => Future.failed(OrganizationNotFoundException(s"Application ${application.id}:${application.label} might be orphan"))
                              case 1 =>
                                val orgDef = result._1(0)._2
                                for (
                                  organization <- Right(new OrganizationStore()
                                    .makeOrganization
                                    .setId(orgDef(0))
                                    .setLabel(orgDef(1))
                                    .setCreatedAt(Utils.timestampFromString(orgDef(6)) match {
                                      case createdAt: Timestamp => createdAt
                                      case _ => null
                                    })
                                    .setUpdatedAt(Utils.timestampFromString(orgDef(7)) match {
                                      case updatedAt: Timestamp => updatedAt
                                      case _ => null
                                    })
                                  ) flatMap { organization =>
                                    Try(orgDef(2).toBoolean) match {
                                      case Success(queryable) =>
                                        if (queryable) Right(organization.setQueryable())
                                        else Right(organization.setUnqueryable())
                                      case Failure(_) => Right(organization)
                                    }
                                  } flatMap { organization =>
                                    result._2.partition(_._1 == "ORGTYPE") match {
                                      case result =>
                                        result._1.length match {
                                          case 0 =>
                                          case 1 =>
                                            val orgTypeDef = result._1(0)._2
                                            for (
                                              orgType <- Right(new OrganizationTypeStore()
                                                .makeOrganizationType
                                                .setId(orgTypeDef(1))
                                                .setLabelTextId(orgTypeDef(2))
                                                .setCreatedAt(Try(Utils.timestampFromString(orgTypeDef(3))) match {
                                                  case Success(createdAt) => createdAt
                                                  case _ => null
                                                })
                                                .setUpdatedAt(Try(Utils.timestampFromString(orgTypeDef(4))) match {
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
                                            ) yield organization.setType(orgType)
                                          case _ =>
                                        }
                                    }
                                    Right(organization)
                                  };
                                  licenseStore = orgDef(4);
                                  licenseFileId = orgDef(5);
                                  orgAppStatus <- Right {
                                    OrganizationApplication.Status.fromInt(orgDef(3).toInt) match {
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
                                )
                              case _ => Future.failed(DuplicateOrganizationException(s"Application ${application.id}:${application.label} has duplicate organization relation"))
                            }
                        }
                      })
                    case None =>
                  }

                  groupedRows.get("PERMISSION") match {
                    case Some(permissionReflections) =>
                      permissionReflections.foreach({ permissionReflection =>
                        permissionReflection.partition(_._1 == "PERMISSION") match {
                          case result =>
                            result._1.length match {
                              case 0 =>
                              case _ =>
                                val permissionDef = result._1(0)._2
                                for (
                                  permission <- Right(new PermissionStore()
                                    .makePermission
                                    .setId(permissionDef(0))
                                    .setKey(permissionDef(1))
                                    .setLabelTextId(permissionDef(2))
                                    .setDescriptionTextId(permissionDef(3))
                                    .setRelatedApplication(application)
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
                                      case Success(editable) =>
                                        if (editable) Right(permission.setEditable)
                                        else Right(permission.setReadonly)
                                      case Failure(_) => Right(permission)
                                    }
                                  } flatMap { permission =>
                                    result._2.partition(_._1 == "PERMISSION_LABEL_LANG_VARIANT") match {
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
                                    }
                                    Right(permission)
                                  }
                                ) yield application.addPermission(permission)
                            }
                        }
                      })
                    case None =>
                  }
                  Right(application)
                }
              ) yield application).getOrElse(null)
            case None =>
          }
        })
        Future.successful(applications.toList.asInstanceOf[List[Application]])
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
          case 0 => Future.failed(NoEntryException("Application store is empty"))
          case _ => Future.successful(applications)
        }
      case Failure(cause) => throw cause
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
          case 0 => Future.failed(ApplicationNotFoundException(s"Application $id couldn't be found"))
          case 1 => Future.successful(applications.head)
          case _ => Future.failed(new DuplicateApplicationException)
        }
      case Failure(cause) => Future.failed(cause)
    })
  }

  def persistApplication(application: Application)(implicit ec: ExecutionContext): Future[Unit] = {
    val transaction = makeTransaction
    transaction match {
      case Success(_) =>
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
                  case Success(organizations) =>
                    val orgs = organizations zip application.organizations.sortBy(_._2._2.id).map(_._2._1)
                    Future.successful(orgs.flatMap({ o =>
                      try {
                        Some(o._1.addApplication(application, o._2))
                      } catch {
                        case _: RelationAlreadyExistsException => None
                      }
                    }))
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
                  case Success(organizations) =>
                    Future.successful(organizations.map({ o =>
                      o.removeApplication(application)
                    }))
                  case Failure(cause) => Future.failed(cause)
                })
              )
            ).transformWith({
              case Success(organizations) =>
                organizationStore.bulkPersistOrganizations(organizations.flatten)
              case Failure(cause) => Future.failed(cause)
            })
          )
        ).transformWith({
          case Success(_) =>
            commitTransaction(transaction).transformWith({
              case Success(_) => Future.unit
              case Failure(cause) => Future.failed(ApplicationNotPersistedException(cause))
            })
          case Failure(cause) =>
            rollbackTransaction(transaction)
            Future.failed(ApplicationNotPersistedException(cause))
        })
      case Failure(cause) => Future.failed(ApplicationNotPersistedException(cause))
    }
  }

  def bulkPersistApplications(applications: List[Application])(implicit ec: ExecutionContext): Future[Unit] = {
    val transaction = makeTransaction
    transaction match {
      case Success(_) =>
        val organizationStore = new OrganizationStore()
        Future.sequence(
          List(
            Utils.igniteToScalaFuture(igniteCache.putAllAsync(
              (applications.map(_.id) zip applications).toMap[UUID, Application].asJava
            )),
            Future.sequence(
              applications.flatMap { application =>
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
                    case Success(organizations) =>
                      val orgs = organizations zip application.organizations.sortBy(_._2._2.id).map(_._2._1)
                      Future.successful(orgs.flatMap({ o =>
                        try {
                          Some(o._1.addApplication(application, o._2))
                        } catch {
                          case _: RelationAlreadyExistsException => None
                        }
                      }))
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
                    case Success(organizations) =>
                      Future.successful(organizations.map({ o =>
                        o.removeApplication(application)
                      }))
                    case Failure(cause) => Future.failed(cause)
                  })
                )
              }
            ).transformWith({
              case Success(organizations) =>
                organizationStore.bulkPersistOrganizations(organizations.flatten)
              case Failure(cause) => Future.failed(cause)
            })
          )
        ).transformWith({
          case Success(_) =>
            commitTransaction(transaction).transformWith({
              case Success(_) => Future.unit
              case Failure(cause) => Future.failed(ApplicationNotPersistedException(cause))
            })
          case Failure(cause) =>
            rollbackTransaction(transaction)
            Future.failed(ApplicationNotPersistedException(cause))
        })
      case Failure(cause) => Future.failed(ApplicationNotPersistedException(cause))
    }
  }

  def deleteApplication(application: Application)(implicit ec: ExecutionContext): Future[Unit] = {
    val transaction = makeTransaction
    transaction match {
      case Success(_) =>
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
              case Success(organizations) =>
                organizationStore.bulkPersistOrganizations(
                  organizations.map(_.removeApplication(application))
                )
              case Failure(cause) => Future.failed(cause)
            })
          )
        ).transformWith({
          case Success(_) =>
            commitTransaction(transaction).transformWith({
              case Success(_) => Future.unit
              case Failure(cause) => Future.failed(ApplicationNotPersistedException(cause))
            })
          case Failure(cause) =>
            rollbackTransaction(transaction)
            Future.failed(ApplicationNotPersistedException(cause))
        })
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
                                     orderBy: List[(String, Int)] = List()
                                   ) extends GetEntityFilters
}