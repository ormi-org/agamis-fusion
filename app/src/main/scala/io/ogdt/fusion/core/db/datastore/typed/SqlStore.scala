package io.ogdt.fusion.db.drivers.typed

import io.ogdt.fusion.core.db.ignite.IgniteClientNodeWrapper

import scala.reflect._

import scala.collection.mutable.Map
import scala.collection.mutable.Buffer

import scala.collection.JavaConverters._

import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.query.SqlFieldsQuery

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

import org.slf4j.LoggerFactory
import org.apache.ignite.cache.query.FieldsQueryCursor
import scala.collection.parallel.mutable.ParArray
import org.apache.ignite.transactions.Transaction
import java.util.UUID
import org.apache.ignite.cache.CacheMode
import io.ogdt.fusion.env.EnvContainer

object SqlStore {
    trait Model {
        // TODO : trouver un moyen de valider la covariance
        // protected val store: SqlStore[UUID, Model]

        genId()

        protected var _id: UUID
        private def genId() = {
            _id = UUID.randomUUID()
        }
    }

    class SqlStoreQuery(var query: String) {
        var params: Array[_] = Array()

        def setParam(newParams: Array[_]): SqlStoreQuery = {
            params = newParams
            this
        }
    }
}

abstract class SqlStore[K, M](wrapper: IgniteClientNodeWrapper) {

    implicit val kTag: ClassTag[K]
    implicit val mTag: ClassTag[M]

    import SqlStore.SqlStoreQuery

    val schema: String
    val cache: String
    var igniteCache: IgniteCache[K, M]

    val log = LoggerFactory.getLogger("io.ogdt.fusion.fs")

    // Call init method
    init()

    def init() = {
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
            .setIndexedTypes(kTag.runtimeClass, mTag.runtimeClass)
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
            // for ( i <- 0 to query.getColumnsCount() - 1) {
            //     log.info(s"index: $i / name: ${query.getFieldName(i)}")
            // }
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
