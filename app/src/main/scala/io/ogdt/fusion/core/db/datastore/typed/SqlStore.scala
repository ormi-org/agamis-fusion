package io.ogdt.fusion.db.drivers.typed

import io.ogdt.fusion.core.db.ignite.IgniteClientNodeWrapper

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

object SqlStore {
    trait Model {
        
    }

    class SqlQuery(var query: String) {
        var params: Map[String, String] = Map[String, String]()

        def setParam(key: String, value: String): SqlQuery = {
            params.update(key, value)
            this
        }
    }
}

abstract class SqlStore[Model](wrapper: IgniteClientNodeWrapper) {
    import SqlStore.SqlQuery

    val schema: String
    val cache: String

    val log = LoggerFactory.getLogger("io.ogdt.fusion.fs")

    val igniteCache: IgniteCache[Long, Model]

    def makeQuery(queryString: String): SqlQuery = {
        new SqlQuery(queryString)
    }
    
    def executeQuery(sqlQuery: SqlQuery): Future[ParArray[List[_]]] = {
        var queryString: String = sqlQuery.query
        for((param, value) <- sqlQuery.params) {
            queryString.replaceAll(param, value)
        }
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

    def makeTransaction(): Unit = {

    }

    def commitTransaction(): Unit = {
        
    }

    def rollbackTransaction(): Unit = {

    }
}
