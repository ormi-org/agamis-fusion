package io.ogdt.fusion.core.db.datastores.typed

import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import org.apache.ignite.IgniteCache
import org.apache.ignite.lang.IgniteFuture
import scala.concurrent.Promise
import scala.util.Try
import scala.concurrent.Future

abstract class CacheStore[K, M](implicit wrapper: IgniteClientNodeWrapper) {

    val globalPrefix: String = "io.ogdt.fusion.cached:"
    val cachePrefix: String
    val cache: String
    protected var igniteCache: IgniteCache[K, M]

    protected def init() = {
        if(wrapper.cacheExists(cache)) {
            igniteCache = wrapper.getCache[K, M](cache).withKeepBinary()
        }
    }

    // Generate key for a specific value
    protected def key(subject: M): K

    // Parse key directly from String
    protected def key(subject: String): K = {
        (globalPrefix + cachePrefix + subject).asInstanceOf[K]
    }

    // Util method to convert igniteFutures to Scala Futures
    private def igniteToScalaFuture[T](igniteFuture: IgniteFuture[T]) = {
        val promise = Promise[T]()
        igniteFuture.listen { k =>
            promise.tryComplete(Try(k.get))
        }
        promise.future
    }

    // Check if a value is set for specified key
    def exists(search: K): Future[java.lang.Boolean] = {
        igniteToScalaFuture(igniteCache.containsKeyAsync(search))
    }

    // Put a value for a specified key
    def put(value: M): Future[Void] = {
        igniteToScalaFuture(igniteCache.putAsync(key(value), value))
    }

    // Get the value for specified key
    def get(search: String): Future[M] = {
        igniteToScalaFuture(igniteCache.getAsync(key(search)))
    }
}
