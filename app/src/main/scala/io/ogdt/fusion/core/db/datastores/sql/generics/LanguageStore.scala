package io.ogdt.fusion.core.db.datastores.sql.generics

import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import io.ogdt.fusion.core.db.datastores.typed.SqlStore
import io.ogdt.fusion.core.db.models.sql.generics.Language
import java.util.UUID
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheMode

class LanguageStore(implicit wrapper: IgniteClientNodeWrapper) extends SqlStore[UUID, Language] {

    override val schema: String = "FUSION"
    override val cache: String = s"SQL_${schema}_LANGUAGE"
    override protected var igniteCache: IgniteCache[UUID,Language] = wrapper.cacheExists(cache) match {
        case true => wrapper.getCache[UUID, Language](cache)
        case false => {
            wrapper.createCache[UUID, Language](
                wrapper.makeCacheConfig[UUID, Language]
                .setCacheMode(CacheMode.REPLICATED)
                .setDataRegionName("Fusion")
                .setName(cache)
                .setSqlSchema(schema)
                .setIndexedTypes(classOf[UUID], classOf[Language])
            )
        }
    }
}
