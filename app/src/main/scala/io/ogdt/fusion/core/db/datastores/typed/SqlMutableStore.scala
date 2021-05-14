package io.ogdt.fusion.core.db.datastores.typed

import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import io.ogdt.fusion.core.db.datastores.typed.sql.SqlStoreQuery
import scala.concurrent.{Future, ExecutionContext}
import org.apache.ignite.cache.query.SqlFieldsQuery
import scala.collection.parallel.immutable.ParSeq
import scala.collection.mutable.Buffer
import scala.jdk.CollectionConverters._

abstract class SqlMutableStore[K, M](implicit wrapper: IgniteClientNodeWrapper) extends SqlStore[K, M] {

    def makeQuery(queryString: String): SqlStoreQuery = {
        new SqlStoreQuery(queryString)
    }
    
    def executeQuery(sqlQuery: SqlStoreQuery)(implicit ec: ExecutionContext): Future[ParSeq[List[_]]] = {
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
}
