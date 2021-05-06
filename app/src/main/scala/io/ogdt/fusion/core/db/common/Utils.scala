package io.ogdt.fusion.core.db.common

import scala.concurrent.Promise
import org.apache.ignite.lang.IgniteFuture
import scala.util.Try

object Utils {
    // Util method to convert igniteFutures to Scala Futures
    def igniteToScalaFuture[T](igniteFuture: IgniteFuture[T]) = {
        val promise = Promise[T]()
        igniteFuture.listen { k =>
            promise.tryComplete(Try(k.get))
        }
        promise.future
    }
}
