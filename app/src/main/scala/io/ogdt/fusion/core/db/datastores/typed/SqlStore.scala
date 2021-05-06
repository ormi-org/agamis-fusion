package io.ogdt.fusion.core.db.datastores.typed

import scala.reflect.{ClassTag, classTag}

import scala.collection.mutable.Map
import scala.collection.mutable.Buffer

import scala.jdk.CollectionConverters._

import org.apache.ignite.IgniteCache
import org.apache.ignite.transactions.Transaction
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.cache.query.{SqlFieldsQuery, FieldsQueryCursor}

import scala.util.{Failure, Success}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.parallel.immutable.ParSeq

import org.slf4j.LoggerFactory

import io.ogdt.fusion.env.EnvContainer
import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import io.ogdt.fusion.core.db.datastores.typed.sql.SqlStoreQuery

abstract class SqlStore[K: ClassTag, M: ClassTag](implicit wrapper: IgniteClientNodeWrapper) {

    val schema: String
    val cache: String
    protected var igniteCache: IgniteCache[K, M]

    protected def init(additionalQueries: List[SqlStoreQuery] = List()) = {
        if(wrapper.cacheExists(cache)) {
            igniteCache = wrapper.getCache[K, M](cache)
        } else {
            var userCacheCfg = wrapper.makeCacheConfig[K, M]()
            userCacheCfg
            .setCacheMode(CacheMode.REPLICATED)
            .setDataRegionName("Fusion")
            .setName(cache)
            .setSqlSchema(schema)
            // .setBackups(EnvContainer.getString("fusion.core.db.ignite.backups").toInt)
            .setIndexedTypes(classTag[K].runtimeClass, classTag[M].runtimeClass)
            igniteCache = wrapper.createCache[K, M](userCacheCfg)
            additionalQueries.foldLeft(Future(List.empty[Unit])) { (prevFuture, query) =>
                for {
                    prev <- prevFuture
                    curr <- executeQuery(query).transformWith({
                        case Success(value) => Future.successful() // LOG success/failure
                        case Failure(cause) => Future.failed(cause) // LOG failure
                    })
                } yield prev :+ curr
            }
        }
    }

    def makeQuery(queryString: String): SqlStoreQuery = {
        new SqlStoreQuery(queryString)
    }
    
    def executeQuery(sqlQuery: SqlStoreQuery): Future[ParSeq[List[_]]] = {
        var queryString: String = sqlQuery.query
        Future {
            var igniteQuery = new SqlFieldsQuery(queryString)
            if (sqlQuery.params.length > 0) igniteQuery.setArgs(sqlQuery.params:_*)
            var query = igniteCache.query(igniteQuery)
            var scalaRes = Buffer[List[_]]()
            query.getAll().forEach(item => {
                scalaRes.addOne(item.asScala.toList)
            })
            ParSeq.fromSpecific(scalaRes)
        }
    }

    // def makeTransaction(): Transaction = {
        
    // }

    // def commitTransaction(): Unit = {
        
    // }

    // def rollbackTransaction(tx: Transaction): Unit = {

    // }
}
