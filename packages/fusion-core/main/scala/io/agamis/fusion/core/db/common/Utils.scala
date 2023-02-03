package io.agamis.fusion.core.db.common

import scala.concurrent.{Future, Promise}
import org.apache.ignite.lang.IgniteFuture

import scala.util.Try
import java.time.Instant
import java.sql.Timestamp

object Utils {
    // Util method to convert igniteFutures to Scala Futures
    def igniteToScalaFuture[T](igniteFuture: IgniteFuture[T]): Future[T] = {
        val promise = Promise[T]()
        igniteFuture.listen { k =>
            promise.tryComplete(Try(k.get))
        }
        promise.future
    }

    // Util method to convert h2 timestamp to sql
    def timestampFromString(timestamp: String): Timestamp = {
        val format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        Timestamp.from(Instant.ofEpochMilli(format.parse(timestamp).getTime))
    }
}
