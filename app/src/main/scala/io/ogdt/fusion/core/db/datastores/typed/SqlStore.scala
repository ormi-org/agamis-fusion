package io.ogdt.fusion.core.db.datastores.typed

import scala.reflect._

import scala.collection.mutable.Map
import scala.collection.mutable.Buffer

import scala.collection.JavaConverters._

import org.apache.ignite.IgniteCache
import org.apache.ignite.transactions.Transaction
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.cache.query.{SqlFieldsQuery, FieldsQueryCursor}

import scala.util.{Failure, Success}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.parallel.mutable.ParArray

import org.slf4j.LoggerFactory

import java.util.UUID

import io.ogdt.fusion.env.EnvContainer
import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import io.ogdt.fusion.core.db.datastores.typed.sql.SqlStoreQuery

abstract class SqlStore[K: ClassTag, M: ClassTag](implicit wrapper: IgniteClientNodeWrapper) {

    val schema: String
    val cache: String
    var igniteCache: IgniteCache[K, M]

    protected def init() = {
        if(wrapper.cacheExists(cache)) {
            igniteCache = wrapper.getCache[K, M](cache)
        } else {
            var userCacheCfg = wrapper.makeCacheConfig[K, M]()
            userCacheCfg
            .setCacheMode(CacheMode.PARTITIONED)
            .setDataRegionName("Fusion")
            .setName(cache)
            .setSqlSchema(schema)
            .setBackups(EnvContainer.getString("fusion.core.db.ignite.backups").toInt)
            .setIndexedTypes(classTag[K].runtimeClass, classTag[M].runtimeClass)
            igniteCache = wrapper.createCache[K, M](userCacheCfg)
        }
    }

    def makeQuery(queryString: String): SqlStoreQuery = {
        new SqlStoreQuery(queryString)
    }
    
    def executeQuery(sqlQuery: SqlStoreQuery): Future[ParArray[List[_]]] = {
        var queryString: String = sqlQuery.query
        Future {
            var query = igniteCache.query(new SqlFieldsQuery(queryString))
            var res = query.getAll()
            var scalaRes = Buffer[List[_]]()
            res.forEach(item => {
                scalaRes.addOne(item.asScala.toList)
            })
            ParArray.fromIterables(scalaRes)
        }
    }

    // def makeTransaction(): Transaction = {
        
    // }

    // def commitTransaction(): Unit = {
        
    // }

    // def rollbackTransaction(tx: Transaction): Unit = {

    // }
}
