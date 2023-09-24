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

class OrganizationStore(implicit wrapper: IgniteClientNodeWrapper)
    extends CacheStore[UUID, Organization] {

    override val schema: String = "FUSION"
    override val cache: String  = s"SQL_${schema}_ORGANIZATION"

    override protected var igniteCache: IgniteCache[UUID, Organization] =
        if (wrapper.cacheExists(cache)) {
            wrapper.getCache[UUID, Organization](cache)
        } else {
            wrapper.createCache[UUID, Organization](
              wrapper
                  .makeCacheConfig[UUID, Organization]
                  .setCacheMode(CacheMode.PARTITIONED)
                  .setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL)
                  .setDataRegionName("Fusion")
                  .setName(cache)
                  .setSqlSchema(schema)
                  .setIndexedTypes(classOf[UUID], classOf[Organization])
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
    def getById(id: String): Any = {
        val filter: IgniteBiPredicate[UUID, Organization] = (k, o) => {
            o.id.equals(UUID.fromString(id))
        }
        this.igniteCache.query(new ScanQuery(filter))
    }
}
