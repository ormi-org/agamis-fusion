package io.agamis.fusion.core.db.datastore.cache

import io.agamis.fusion.core.db.datastore.cache.exceptions.NotFoundException
import io.agamis.fusion.core.db.datastore.typed.CacheStore
import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import io.agamis.fusion.core.model.Organization
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheAtomicityMode
import org.apache.ignite.cache.CacheMode

import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class OrganizationStore(implicit
    wrapper: IgniteClientNodeWrapper,
    ec: ExecutionContext
) extends CacheStore[UUID, Organization] {

    override val schema: String = "FUSION"
    override val cache: String  = s"${schema}_ORGANIZATION"

    override protected var igniteCache: IgniteCache[UUID, Organization] =
        wrapper.cacheExists(cache) match {
            case true =>
                wrapper.getCache[UUID, Organization](cache)
            case false =>
                wrapper
                    .createCache[UUID, Organization](
                      wrapper
                          .makeCacheConfig[UUID, Organization]
                          .setCacheMode(CacheMode.PARTITIONED)
                          .setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL)
                          .setDataRegionName("Fusion")
                          .setName(cache)
                    )
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
        Future {
            this.igniteCache.getAsync(id).get()
        } transformWith { o =>
            Option.apply(o.get) match {
                case Some(value) => Future.successful(value)
                case None =>
                    Future.failed(
                      NotFoundException(
                        s"Organisation ${id} not found"
                      )
                    )
            }
        }
    }
}
