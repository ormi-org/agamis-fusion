package io.ogdt.fusion.core.db.datastores.sql

import io.ogdt.fusion.core.db.datastores.typed.SqlMutableStore

import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import java.util.UUID
import io.ogdt.fusion.core.db.models.sql.OrganizationType
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheMode
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import io.ogdt.fusion.core.db.common.Utils
import scala.util.Success
import scala.util.Failure
import io.ogdt.fusion.core.db.datastores.sql.exceptions.organizationtypes.{
    OrganizationtypeNotPersistedException
}
import org.apache.ignite.cache.CacheAtomicityMode
import io.ogdt.fusion.core.db.datastores.sql.generics.TextStore
import io.ogdt.fusion.core.db.models.sql.generics.Language
import io.ogdt.fusion.core.db.datastores.typed.sql.GetEntityFilters
import io.ogdt.fusion.core.db.datastores.typed.sql.SqlStoreQuery
import scala.collection.mutable.ListBuffer
import java.sql.Timestamp
import io.ogdt.fusion.core.db.datastores.sql.exceptions.organizationtypes.OrganizationtypeQueryExecutionException
import io.ogdt.fusion.core.db.datastores.sql.generics.exceptions.texts.TextNotFoundException
import scala.util.Try
import io.ogdt.fusion.core.db.datastores.sql.exceptions.NoEntryException
import io.ogdt.fusion.core.db.datastores.sql.exceptions.organizationtypes.DuplicateOrganizationtypeException
import io.ogdt.fusion.core.db.datastores.sql.exceptions.organizationtypes.OrganizationtypeNotFoundException

class OrganizationTypeStore(implicit wrapper: IgniteClientNodeWrapper) extends SqlMutableStore[UUID, OrganizationType] {

    override val schema: String = "FUSION"
    override val cache: String = s"SQL_${schema}_ORGANIZATIONTYPE"
    override protected var igniteCache: IgniteCache[UUID,OrganizationType] = wrapper.cacheExists(cache) match {
        case true => wrapper.getCache[UUID, OrganizationType](cache)
        case false => {
            wrapper.createCache[UUID, OrganizationType](
                wrapper.makeCacheConfig[UUID, OrganizationType]
                .setCacheMode(CacheMode.REPLICATED)
                .setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL)
                .setDataRegionName("Fusion")
                .setName(cache)
                .setSqlSchema(schema)
                .setIndexedTypes(classOf[UUID], classOf[OrganizationType])
            )
        }
    }

    def makeOrganizationType: OrganizationType = {
        implicit val organizationTypeStore: OrganizationTypeStore = this
        new OrganizationType
    }

    def makeOrganizationTypeQuery(queryFilters: OrganizationTypeStore.GetOrganizationTypesFilters): SqlStoreQuery = {
        var baseQueryString = queryString.replace("$schema", schema)
        var queryArgs: ListBuffer[String] = ListBuffer()
        var whereStatements: ListBuffer[String] = ListBuffer()
        queryFilters.filters.foreach({ filter =>
            var innerWhereStatement: ListBuffer[String] = ListBuffer()
            // manage ids search
            if (!filter.id.isEmpty) {
                innerWhereStatement += s"orgtype_id in (${(for (i <- 1 to filter.id.length) yield "?").mkString(",")})"
                queryArgs ++= filter.id
            }
            // manage labels search
            if (!filter.label.isEmpty) {
                innerWhereStatement += s"orgtype_label in (${(for (i <- 1 to filter.label.length) yield "?").mkString(",")})"
                queryArgs ++= filter.label
            }
            // manage metadate search
            filter.createdAt match {
                case Some((test, time)) => {
                    innerWhereStatement += s"orgtype_created_at ${
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
                    innerWhereStatement += s"orgtype_updated_at ${
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
                s"orgtype_${o._1} ${o._2 match {
                    case 1 => "ASC"
                    case -1 => "DESC"
                }}"
            ).mkString(", ")}"
        }
        makeQuery(baseQueryString)
        .setParams(queryArgs.toList)
    }

    def getOrganizationTypes(queryFilters: OrganizationTypeStore.GetOrganizationTypesFilters)(implicit ec: ExecutionContext): Future[List[OrganizationType]] = {
        executeQuery(
            makeOrganizationTypeQuery(queryFilters)
        ).transformWith({
            case Success(rows) => {
                val entityReflections = rows.groupBy(_(0))
                val organizationTypes = rows.map(_(0)).distinct.map(entityReflections.get(_).get).map(entityReflection => {
                    val groupedRows = getRelationsGroupedRowsFrom(entityReflection, 4, 5)
                    groupedRows.get("ORGTYPE") match {
                        case Some(organizationTypeReflections) => {
                            val orgTypeDef = organizationTypeReflections.head.head._2
                            (for (
                                organizationType <- Right(makeOrganizationType
                                    .setId(orgTypeDef(0).toString)
                                    .setLabelTextId(orgTypeDef(1).toString)
                                    .setCreatedAt(Utils.timestampFromString(orgTypeDef(2)) match {
                                        case createdAt: Timestamp => createdAt
                                        case _ => null
                                    })
                                    .setUpdatedAt(Utils.timestampFromString(orgTypeDef(3)) match {
                                        case updatedAt: Timestamp => updatedAt
                                        case _ => null
                                    })
                                ) flatMap { organizationType => // mapping labels
                                    organizationTypeReflections.head.filter(_._1 == "ORGTYPE_LABEL_LANG_VARIANT") match {
                                        case result => {
                                            result.isEmpty match {
                                                case true => Future.failed(TextNotFoundException(s"Organization type ${organizationType.id} might lack of label in any language"))
                                                case false => {
                                                    result.foreach({ orgTypeLangVariant =>
                                                        val orgTypeLangVariantDef = orgTypeLangVariant._2
                                                        organizationType.setLabel(
                                                            Language.apply
                                                            .setId(orgTypeLangVariantDef(3))
                                                            .setCode(orgTypeLangVariantDef(2))
                                                            .setLabel(orgTypeLangVariantDef(4)),
                                                            orgTypeLangVariantDef(1)
                                                        )
                                                    })
                                                }
                                            }
                                        }
                                    }
                                    Right(organizationType)
                                } flatMap { organizationType => // mapping relations
                                    groupedRows.get("ORGANIZATION") match {
                                        case Some(organizationReflections) => {
                                            organizationReflections.foreach({ organizationReflection =>
                                                val orgDef = organizationReflection(0)._2
                                                // ORG.id, ORG.label, ORG.queryable, ORG.organizationtype_id, ORG.created_at, ORG.updated_at
                                                (for (
                                                    organization <- Right(new OrganizationStore()
                                                        .makeOrganization
                                                        .setId(orgDef(0))
                                                        .setLabel(orgDef(1))
                                                        .setCreatedAt(Utils.timestampFromString(orgDef(4)) match {
                                                            case createdAt: Timestamp => createdAt
                                                            case _ => null
                                                        })
                                                        .setUpdatedAt(Utils.timestampFromString(orgDef(5)) match {
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
                                                    }
                                                ) yield organizationType.addRelatedOrganization(organization))
                                            })
                                        }
                                        case None => {}
                                    }
                                    Right(organizationType)
                                }
                            ) yield organizationType).getOrElse(null)
                        }
                        case None => {}
                    }
                })
                Future.successful(organizationTypes.toList.asInstanceOf[List[OrganizationType]])
            }
            case Failure(cause) => Future.failed(OrganizationtypeQueryExecutionException(cause))
        })
    }

    def getAllOrganizationTypes(implicit ec: ExecutionContext): Future[List[OrganizationType]] ={
        getOrganizationTypes(OrganizationTypeStore.GetOrganizationTypesFilters().copy(
            orderBy = List(
                ("id", 1)
            )
        )).transformWith({
            case Success(organizationTypes) => 
                organizationTypes.isEmpty match {
                    case true => Future.failed(new NoEntryException("OrganizationType store is empty"))
                    case false => Future.successful(organizationTypes)
                }
            case Failure(cause) => Future.failed(cause)
        })
    }

    def getOrganizationTypeById(id: String)(implicit ec: ExecutionContext): Future[OrganizationType] = {
        getOrganizationTypes(
            OrganizationTypeStore.GetOrganizationTypesFilters().copy(
                filters = List(
                    OrganizationTypeStore.GetOrganizationTypesFilter().copy(
                        id = List(id)
                    )
                )
            )
        ).transformWith({
            case Success(organizationTypes) => 
                organizationTypes.length match {
                    case 0 => Future.failed(new OrganizationtypeNotFoundException(s"Organization type ${id} couldn't be found"))
                    case 1 => Future.successful(organizationTypes.head)
                    case _ => Future.failed(new DuplicateOrganizationtypeException)
                }
            case Failure(cause) => Future.failed(cause)
        })
    }

    def persistOrganizationType(organizationType: OrganizationType)(implicit ec: ExecutionContext): Future[Unit] = {
        val transaction = makeTransaction
        transaction match {
            case Success(tx) => {
                Utils.igniteToScalaFuture(igniteCache.putAsync(
                    organizationType.id, organizationType
                )).transformWith({
                    case Success(value) => {
                        var textStore = new TextStore
                        Future.sequence(
                            organizationType.labels.map({ label =>
                                textStore.makeText
                                .setId(label._1._1.toString)
                                .setRelatedLanguage(
                                    Language.apply
                                    .setId(label._1._2.toString)
                                    .setCode(label._2._1)
                                ).setContent(label._2._2)
                            }).map({ text => 
                                textStore.updateText(text)
                            }
                        )).transformWith({
                            case Success(value) => {
                                commitTransaction(transaction).transformWith({
                                    case Success(value) => Future.unit
                                    case Failure(cause) => Future.failed(OrganizationtypeNotPersistedException(cause))
                                })
                            }
                            case Failure(cause) => {
                                rollbackTransaction(transaction)
                                Future.failed(OrganizationtypeNotPersistedException(cause))
                            }
                        })
                    }
                    case Failure(cause) => {
                        rollbackTransaction(transaction)
                        Future.failed(OrganizationtypeNotPersistedException(cause))
                    }
                })
            }
            case Failure(cause) => Future.failed(OrganizationtypeNotPersistedException(cause))
        }
    }

    def deleteOrgnizationType(organizationType: OrganizationType)(implicit ec: ExecutionContext): Future[Unit] = {
        if (!organizationType.relatedOrganizations.isEmpty) return Future.failed(OrganizationtypeNotPersistedException("organizationType is still typifying some organization"))
        val transaction = makeTransaction
        transaction match {
            case Success(tx) => {
                Utils.igniteToScalaFuture(igniteCache.removeAsync(organizationType.id))
                .transformWith({
                    case Success(value) => {
                        var textStore = new TextStore
                        Future.sequence(organizationType.labels.map({ label =>
                            textStore.deleteText(s"${label._1._1}:${label._1._2}")
                        })).transformWith({
                            case Success(value) => {
                                commitTransaction(transaction).transformWith({
                                    case Success(value) => Future.unit
                                    case Failure(cause) => throw cause
                                })
                            }
                            case Failure(cause) => {
                                rollbackTransaction(transaction)
                                throw cause
                            }
                        })
                    }
                    case Failure(cause) => Future.failed(OrganizationtypeNotPersistedException(cause))
                })
            }
            case Failure(cause) => Future.failed(OrganizationtypeNotPersistedException(cause))
        }
    }
}

object OrganizationTypeStore {
    case class GetOrganizationTypesFilter(
        id: List[String] = List(),
        label: List[String] = List(),
        createdAt: Option[(String, Timestamp)] = None, // (date, (eq, lt, gt, ne))
        updatedAt: Option[(String, Timestamp)] = None // (date, (eq, lt, gt, ne))
    )
    case class GetOrganizationTypesFilters(
        filters: List[GetOrganizationTypesFilter] = List(),
        orderBy: List[(String, Int)] = List() // (column, direction)
    ) extends GetEntityFilters
}
