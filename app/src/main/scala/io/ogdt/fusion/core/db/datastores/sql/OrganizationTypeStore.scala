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

    def persistOrganizationType(organizationType: OrganizationType)(implicit ec: ExecutionContext): Future[Unit] = {
        val transaction = makeTransaction
        transaction match {
            case Success(tx) => {
                Utils.igniteToScalaFuture(igniteCache.putAsync(
                    organizationType.id, organizationType
                )).transformWith({
                    case Success(value) => {
                        var textStore = new TextStore
                        Future.sequence(organizationType.labels.map({ label =>
                            textStore.makeText
                            .setId(label._1._1.toString)
                            .setRelatedLanguage(
                                Language.apply
                                .setId(label._1._2.toString)
                                .setCode(label._2._1)
                            ).setContent(label._2._2)
                        }).map({ text => 
                            textStore.updateText(text)
                        })).transformWith({
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
