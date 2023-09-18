package io.agamis.fusion.core.db.datastores.typed

import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import org.apache.ignite.IgniteCache
import scala.concurrent.Future
import scala.jdk.CollectionConverters._
import scala.language.postfixOps
import io.agamis.fusion.core.db.common.Utils

abstract class CacheStore[K, M](implicit wrapper: IgniteClientNodeWrapper) {

    val globalPrefix: String = "io.agamis.fusion.cached:"
    val cachePrefix: String
    val cache: String
    protected var igniteCache: IgniteCache[K, M]

    protected def init() = {
        if (wrapper.cacheExists(cache)) {
            igniteCache = wrapper.getCache[K, M](cache)
        }
    }

    // Generate key for a specific value
    protected def key(subject: M): K

    // Parse key directly from String
    def key(subject: String): K = {
        (globalPrefix + cachePrefix + subject).asInstanceOf[K]
    }

    // Check if a value is set for specified key
    def exists(search: K): Future[java.lang.Boolean] = {
        Utils.igniteToScalaFuture(igniteCache.containsKeyAsync(search))
    }

    // Put a value for a specified key
    def put(value: M): Future[Void] = {
        Utils.igniteToScalaFuture(igniteCache.putAsync(key(value), value))
    }

    // Put many values for specified keys
    def putMany(values: List[M]): Future[_] = {
        Utils.igniteToScalaFuture(igniteCache.putAllAsync(
            (values map (value => key(value) -> value) toMap ).asJava
        ))
    }

    // Get the value for specified key
    def get(search: String): Future[M] = {
        Utils.igniteToScalaFuture(igniteCache.getAsync(key(search)))
    }

    def getMany(searches: List[String]): Future[java.util.Map[K, M]] = {
        Utils.igniteToScalaFuture(igniteCache.getAllAsync(
            searches.map(search => {
                key(search)
            }).toSet.asJava
        ))
    }

    def delete(value: M): Future[java.lang.Boolean] = {
        Utils.igniteToScalaFuture(igniteCache.removeAsync(key(value)))
    }

    def deleteMany(values: List[M]): Future[Void] = {
        Utils.igniteToScalaFuture(igniteCache.removeAllAsync(
            values.map(value => {
                key(value)
            }).toSet.asJava
        ))
    }
}
