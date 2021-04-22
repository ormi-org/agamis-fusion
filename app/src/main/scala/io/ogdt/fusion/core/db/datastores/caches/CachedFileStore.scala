package io.ogdt.fusion.core.db.datastores.caches

import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import io.ogdt.fusion.core.db.datastores.typed.CacheStore
import io.ogdt.fusion.core.db.datastores.models.documents.File

import org.apache.ignite.IgniteCache

class CachedFileStore(implicit wrapper: IgniteClientNodeWrapper) extends CacheStore[String, File] {

    override val cache: String = "Cache"

    override val cachePrefix: String = "file:"

    override protected var igniteCache: IgniteCache[String, File] = null

    super .init()

    protected def key(subject: File): String = {
        globalPrefix + cachePrefix + subject.id.toString()
    }
}
