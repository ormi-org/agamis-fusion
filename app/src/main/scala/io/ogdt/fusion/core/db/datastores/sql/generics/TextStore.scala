package io.ogdt.fusion.core.db.datastores.sql.generics

import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import io.ogdt.fusion.core.db.datastores.typed.SqlStore
import io.ogdt.fusion.core.db.models.sql.generics.Text
import java.util.UUID
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheMode

class TextStore(implicit wrapper: IgniteClientNodeWrapper) extends SqlStore[String, Text] {

    override val schema: String = "FUSION"
    override val cache: String = s"SQL_${schema}_TEXT"
    override protected var igniteCache: IgniteCache[String,Text] = wrapper.cacheExists(cache) match {
        case true => wrapper.getCache[String, Text](cache)
        case false => {
            wrapper.createCache[String, Text](
                wrapper.makeCacheConfig[String, Text]
                .setCacheMode(CacheMode.REPLICATED)
                .setDataRegionName("Fusion")
                .setName(cache)
                .setSqlSchema(schema)
                .setIndexedTypes(classOf[String], classOf[Text])
            )
        }
    }
}
