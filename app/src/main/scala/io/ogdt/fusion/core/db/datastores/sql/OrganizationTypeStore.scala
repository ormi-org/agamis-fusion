package io.ogdt.fusion.core.db.datastores.sql

import io.ogdt.fusion.core.db.datastores.typed.SqlMutableStore

import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import java.util.UUID
import io.ogdt.fusion.core.db.models.sql.OrganizationType
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheMode

class OrganizationTypeStore(implicit wrapper: IgniteClientNodeWrapper) extends SqlMutableStore[UUID, OrganizationType] {

    override val schema: String = "FUSION"
    override val cache: String = s"SQL_${schema}_ORGANIZATIONTYPE"
    override protected var igniteCache: IgniteCache[UUID,OrganizationType] = wrapper.cacheExists(cache) match {
        case true => wrapper.getCache[UUID, OrganizationType](cache)
        case false => {
            wrapper.createCache[UUID, OrganizationType](
                wrapper.makeCacheConfig[UUID, OrganizationType]
                .setCacheMode(CacheMode.REPLICATED)
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
}
