package io.ogdt.fusion.core.db.datastores.typed

import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import io.ogdt.fusion.core.db.datastores.typed.sql.SqlStoreQuery
import scala.concurrent.{Future, ExecutionContext}
import org.apache.ignite.cache.query.SqlFieldsQuery
import scala.collection.parallel.immutable.ParSeq
import scala.collection.mutable.Buffer
import scala.collection.immutable.Iterable
import scala.jdk.CollectionConverters._

abstract class SqlMutableStore[K, M](implicit wrapper: IgniteClientNodeWrapper) extends SqlStore[K, M] {

    /** A method to generate a new SqlStoreQuery instance
      *
      * @param queryString the plain SQL query
      * @return a [[io.ogdt.fusion.core.db.datastores.typed.sql.SqlStoreQuery SqlStoreQuery]] instance
      */
    def makeQuery(queryString: String): SqlStoreQuery = {
        new SqlStoreQuery(queryString)
    }
    
    /** A method for executing a SQL query stored in a SqlStoreQuery instance
      *
      * @param sqlQuery the SQL query to execute in Ignite cache
      * @param ec implicit [[scala.concurrent.ExecutionContext ExecutionContext]]
      * @return a future result containing parallel sequence of rows
      */
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

    /** A method for transforming plain relation rows into grouped data set
      * 
      * Result is grouped by related entity ID and then by data type (eg : ORGANIZATION, PROFILE, USER...)
      * Structure : Map("<RELATION_TYPE>", [
      *     Row list (all rows that reflects relation and its sub-relations data) :
      *     (DATA_TYPE, ROW[FIELDS])
      * ])
      *
      * @param relationRows the plain relation rows list to transform
      * @param dataFieldIndex the index of the field in the row where data are stored
      * @param dataTypeFieldIndex the index of the field in the row where data type is specified
      * @return transformed data ready to load
      */
    protected def getRelationsGroupedRowsFrom(relationRows: List[List[_]], dataFieldIndex: Int, dataTypeFieldIndex: Int): Map[String, Iterable[List[(String, Array[String])]]] = {
        relationRows.map({ entityFields =>
            (entityFields(dataTypeFieldIndex).asInstanceOf[String], entityFields(dataFieldIndex).asInstanceOf[String].split("||"))
        })
        // group by relation id
        .groupBy(_._2(0))
        // map to iterable of entities
        .map(_._2)
        // group entities by types
        .groupBy(_(0)._1)
    }
}
