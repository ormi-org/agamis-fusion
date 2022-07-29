package io.agamis.fusion.core.db.datastores.typed

import io.agamis.fusion.core.db.datastores.typed.sql.SqlStoreQuery
import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import org.apache.ignite.cache.query.SqlFieldsQuery

import java.io.{FileNotFoundException, InputStream}
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.reflect.{ClassTag, classTag}

abstract class SqlMutableStore[K, M: ClassTag](implicit wrapper: IgniteClientNodeWrapper) extends SqlStore[K, M] {

    /**
      * A default query string based on Store M type
      */
    val queryString: String = {
        val classCanonicalName = getClass.getCanonicalName
        val sqlFilePath = classCanonicalName.split("\\.").dropRight(1).mkString("/") + "/" + classTag[M].runtimeClass.getSimpleName.toLowerCase() + ".sql"
        val sqlStreamedFile = getClass.getClassLoader.getResourceAsStream(sqlFilePath)
        sqlStreamedFile match {
            case null => throw new FileNotFoundException()
            case input: InputStream => scala.io.Source.fromInputStream(input).getLines().mkString("\n")
        }
    }

    /** A method to generate a new SqlStoreQuery instance
      *
      * @param queryString the plain SQL query
      * @return a [[io.agamis.fusion.core.db.datastores.typed.sql.SqlStoreQuery SqlStoreQuery]] instance
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
    def executeQuery(sqlQuery: SqlStoreQuery)(implicit ec: ExecutionContext): Future[Vector[List[_]]] = {
        val queryString: String = sqlQuery.query
        Future {
            val igniteQuery = new SqlFieldsQuery(queryString)
            if (sqlQuery.params.nonEmpty) igniteQuery.setArgs(sqlQuery.params:_*)
            val query = igniteCache.query(igniteQuery)
            var scalaRes = Vector[List[_]]()
            query.getAll.forEach(item => {
                scalaRes :+= item.asScala.toList.asInstanceOf[List[_]]
            })
            scalaRes
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
    protected def getRelationsGroupedRowsFrom(relationRows: Vector[List[_]], dataFieldIndex: Int, dataTypeFieldIndex: Int): Map[String, scala.collection.Iterable[Vector[(String, Array[String])]]] = {
        val result = relationRows.map({ entityFields =>
            (entityFields(dataTypeFieldIndex).toString, entityFields(dataFieldIndex).toString.split("""\|\|"""))
        })
        // group by relation id
        .groupBy(_._2(0))
        // map to iterable of entities
        result.values
        // group entities by types
        .groupBy(_(0)._1)
    }
}
