package io.agamis.fusion.core.db.datastore.cache

import io.agamis.fusion.core.db.datastore.typed.CacheStore
import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import io.agamis.fusion.core.model.Organization
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheAtomicityMode
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.cache.query.ScanQuery
import org.apache.ignite.lang.IgniteBiPredicate

import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import io.agamis.fusion.core.db.datastore.cache.exceptions.NotFoundException
import io.agamis.fusion.core.db.datastore.cache.exceptions.DuplicateEntityException

class OrganizationStore(implicit
    wrapper: IgniteClientNodeWrapper,
    ec: ExecutionContext
) extends CacheStore[UUID, Organization] {

    override val schema: String = "FUSION"
    override val cache: String  = s"SQL_${schema}_ORGANIZATION"

    override protected var igniteCache: IgniteCache[UUID, Organization] =
        wrapper.cacheExists(cache) match {
            case true =>
                wrapper.getCache[UUID, Organization](cache).withKeepBinary()
            case false =>
                wrapper
                    .createCache[UUID, Organization](
                      wrapper
                          .makeCacheConfig[UUID, Organization]
                          .setCacheMode(CacheMode.PARTITIONED)
                          .setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL)
                          .setDataRegionName("Fusion")
                          .setName(cache)
                          .setSqlSchema(schema)
                          .setIndexedTypes(classOf[UUID], classOf[Organization])
                    )
                    .withKeepBinary()
        }

    protected def key(subject: Organization): UUID = {
        subject.id
    }

    /** Get organization by id in ignite cache
      *
      * @param id
      *   to filter on
      */
    def getById(id: UUID): Future[Organization] = {
        val filter: IgniteBiPredicate[UUID, Organization] = (k, o) => {
            k.equals(id)
        }
        Future {
            val result = this.igniteCache.query(new ScanQuery(filter)).getAll
            result.size match {
                case 0 =>
                    throw NotFoundException(
                      s"Organisation ${id} not found"
                    )
                case 1 => result.get(0).getValue()
                case size =>
                    throw DuplicateEntityException(
                      s"Found ${size} organisation with ${id}"
                    )
            }
        }
    }
}
