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

import io.ogdt.fusion.core.db.datastores.sql.exceptions.applications.{
    ApplicationNotPersistedException,
    ApplicationNotFoundException,
    DuplicateApplicationException,
    ApplicationQueryExecutionException
}
import io.ogdt.fusion.core.db.common.Utils
import io.ogdt.fusion.core.db.models.sql.generics.exceptions.RelationAlreadyExistsException

class ApplicationStore(implicit wrapper: IgniteClientNodeWrapper) extends SqlMutableStore[UUID, Application] {

    override val schema: String = "FUSION"
    override val cache: String = s"SQL_${schema}_Application"
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
            ""
        var queryArgs: ListBuffer[String] = ListBuffer()
        var whereStatements: ListBuffer[String] = ListBuffer()
        queryFilters.filters.foreach({ filter =>
            var innerWhereStatement: ListBuffer[String] = ListBuffer()
            // manage ids search
            if (!filter.id.isEmpty) {
                innerWhereStatement += s"application_id in (${(for (i <- 1 to filter.id.length) yield "?").mkString(",")})"
                queryArgs ++= filter.id
            }
            // manage appUniversalId search
            if (!filter.appUniversalId.isEmpty) {
                innerWhereStatement += s"application_app_universal_id in (${(for (i <- 1 to filter.id.length) yield "?").mkString(",")})"
                queryArgs ++= filter.appUniversalId
            }
            // manage status search
            if (!filter.status.isEmpty) {
                innerWhereStatement += s"application_status in (${(for (i <- 1 to filter.id.length) yield "?").mkString(",")})"
                queryArgs ++= filter.status.map({ status =>
                    status.toInt.toString
                })
            }
            // manage manifestUrl search
            if (!filter.manifestUrl.isEmpty) {
                innerWhereStatement += s"application_manifest_url in (${(for (i <- 1 to filter.id.length) yield "?").mkString(",")})"
                queryArgs ++= filter.manifestUrl
            }
            // manage storeUrl search
            if (!filter.storeUrl.isEmpty) {
                innerWhereStatement += s"application_store_url in (${(for (i <- 1 to filter.id.length) yield "?").mkString(",")})"
                queryArgs ++= filter.storeUrl
            }
            // manage metadate search
            filter.createdAt match {
                case Some((test, time)) => {
                    innerWhereStatement += s"application_created_at ${
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
                    innerWhereStatement += s"application_updated_at ${
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
                s"application_${o._1} ${o._2 match {
                    case 1 => "ASC"
                    case -1 => "DESC"
                }}"
            ).mkString(", ")}"
        }
        makeQuery(queryString)
        .setParams(queryArgs.toList)
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
                                        OrganizationStore.GetOrganizationsFilters().copy(filters = List(
                                            OrganizationStore.GetOrganizationsFilter()
                                            .copy(id = application.organizations.filter(_._1 == true).map(_._2._2.id.toString))
                                        ))
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
                                        OrganizationStore.GetOrganizationsFilters().copy(filters = List(
                                            OrganizationStore.GetOrganizationsFilter()
                                            .copy(id = application.organizations.filter(_._1 == false).map(_._2._2.id.toString))
                                        ))
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
        id: List[String],
        appUniversalId: List[String],
        status: List[Application.Status],
        manifestUrl: List[String],
        storeUrl: List[String],
        createdAt: Option[(String, Timestamp)],
        updatedAt: Option[(String, Timestamp)]
    )
    case class GetApplicationsFilters(
        filters: List[GetApplicationsFilter],
        orderBy: List[(String,Int)]
    ) extends GetEntityFilters
}