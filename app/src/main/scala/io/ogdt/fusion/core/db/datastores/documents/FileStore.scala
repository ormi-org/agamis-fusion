package io.ogdt.fusion.core.db.datastores.documents

import scala.util.Success
import scala.util.Failure
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import io.ogdt.fusion.core.db.datastores.typed.DocumentStore
import io.ogdt.fusion.core.db.datastores.models.documents.File
import io.ogdt.fusion.core.db.wrappers.mongo.ReactiveMongoWrapper

import reactivemongo.api.DB
import reactivemongo.api.commands.WriteResult
import java.util.UUID
import reactivemongo.api.bson.BSONObjectID
import reactivemongo.api.bson.BSONDocument
import io.ogdt.fusion.core.db.datastores.documents.aggregations.GetFileFromPath
import io.ogdt.fusion.core.db.datastores.documents.aggregations.typed.Pipeline
import reactivemongo.api.bson.Macros
import reactivemongo.api.bson.BSONString
import reactivemongo.api.bson.BSONValue

class FileStore(implicit wrapper: ReactiveMongoWrapper) extends DocumentStore[File] {

    override val database: String = "fusiondb"
    override val collection: String = "files"

    override def insert(docObject: File): Future[WriteResult] = {
        wrapper.getCollection(database, collection).transformWith({
            case Success(col) => {
                col.insert.one[File](docObject)
            }
            case Failure(cause) => throw new Exception(cause)
        })
    }

    override def insertMany(docObject: List[File]): Future[_] = {
        Future.successful()
    }

    override def aggregate(pipeline: Pipeline): Future[List[File]] = {
        wrapper.getCollection(database, collection).transformWith({
            case Success(col) => {
                col.aggregatorContext[File](
                    pipeline = pipeline.get.asInstanceOf[List[col.AggregationFramework.PipelineOperator]]
                ).prepared.cursor.collect[List]()
            }
            case Failure(cause) => throw new Exception(cause)
        })
    }

    def findByID(id: String): Future[Option[File]] = {
        wrapper.getCollection(database, collection).transformWith({
            case Success(col) => {
                try {
                    col.find(BSONDocument("_id" -> BSONObjectID.parse(id).get)).one[File]
                } catch {
                    case e: IllegalArgumentException => throw new Exception(e)
                }
            }
            case Failure(cause) => throw new Exception(cause)
        })
    }

    def findByPath(path: String): Future[File] = {
        wrapper.getCollection(database, collection).transformWith({
            case Success(col) => {
                aggregate(pipeline = GetFileFromPath.pipeline(col).setPath(path)).transformWith({
                    case Success(files) => {
                        wrapper.getLogger().info(files.toString())
                        Future.successful(files(0))
                    }
                    case Failure(cause) => throw new Exception(cause)
                })
            }
            case Failure(cause) => throw new Exception(cause)
        })
    }
}
