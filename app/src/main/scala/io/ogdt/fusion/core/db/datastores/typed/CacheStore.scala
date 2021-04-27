package io.ogdt.fusion.core.db.datastores.typed

import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import org.apache.ignite.IgniteCache
import org.apache.ignite.lang.IgniteFuture
import scala.concurrent.Promise
import scala.util.Try
import scala.concurrent.Future
import scala.collection.JavaConverters._
import scala.util.Success
import scala.util.Failure
import scala.language.postfixOps

abstract class CacheStore[K, M](implicit wrapper: IgniteClientNodeWrapper) {

    val globalPrefix: String = "io.ogdt.fusion.cached:"
    val cachePrefix: String
    val cache: String
    protected var igniteCache: IgniteCache[K, M]

    protected def init() = {
        if(wrapper.cacheExists(cache)) {
            igniteCache = wrapper.getCache[K, M](cache)
        }
    }

    // Generate key for a specific value
    protected def key(subject: M): K

    // Parse key directly from String
    def key(subject: String): K = {
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

    // Put many values for specified keys
    def putMany(values: List[M]): Future[_] = {
        igniteToScalaFuture(igniteCache.putAllAsync(
            (values map (value => key(value) -> value) toMap ).asJava
        ))
    }

    // Get the value for specified key
    def get(search: String): Future[M] = {
        igniteToScalaFuture(igniteCache.getAsync(key(search)))
    }

    def getMany(searches: List[String]): Future[java.util.Map[K, M]] = {
        igniteToScalaFuture(igniteCache.getAllAsync(
            searches.map(search => {
                key(search)
            }).toSet.asJava
        ))
    }

    def delete(value: M): Future[java.lang.Boolean] = {
        igniteToScalaFuture(igniteCache.removeAsync(key(value)))
    }

    def deleteMany(values: List[M]): Future[Void] = {
        igniteToScalaFuture(igniteCache.removeAllAsync(
            values.map(value => {
                key(value)
            }).toSet.asJava
        ))
    }
}
