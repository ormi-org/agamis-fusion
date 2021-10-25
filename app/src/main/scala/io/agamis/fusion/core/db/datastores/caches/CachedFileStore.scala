package io.agamis.fusion.core.db.datastores.caches

import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import io.agamis.fusion.core.db.datastores.typed.CacheStore
import io.agamis.fusion.core.db.models.documents.File

import org.apache.ignite.IgniteCache

class CachedFileStore(implicit wrapper: IgniteClientNodeWrapper) extends CacheStore[String, File] {

    override val cache: String = "Cache"

    override val cachePrefix: String = "file:"

    override protected var igniteCache: IgniteCache[String, File] = _

    super .init()

    override protected def key(subject: File): String = {
        globalPrefix + cachePrefix + subject.id.stringify
    }
}
