package io.ogdt.fusion.core.db.datastores.documents

import scala.util.Success
import scala.util.Failure
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import java.util.UUID

import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.commands.WriteResult

import io.ogdt.fusion.core.db.wrappers.mongo.ReactiveMongoWrapper
import io.ogdt.fusion.core.db.datastores.typed.DocumentStore
import io.ogdt.fusion.core.db.datastores.models.documents.File

import io.ogdt.fusion.core.db.datastores.documents.aggregations.typed.Pipeline
import io.ogdt.fusion.core.db.datastores.documents.aggregations.{
    GetFileFromPath,
    GetFileFromId,
    GetFilesFromId,
    GetFileChildrenFromId
}

class FileStore(implicit wrapper: ReactiveMongoWrapper) extends DocumentStore[File] {

    override val database: String = "fusiondb"
    override val collection: String = "files"

    override def insert(file: File): Future[WriteResult] = {
        wrapper.getCollection(database, collection).transformWith({
            case Success(col) => {
                col.insert.one[File](file).transformWith({
                    case Success(result) => {
                        Future.successful(result)
                    }
                    case Failure(cause) => throw cause
                })
            }
            case Failure(cause) => throw cause
        })
    }

    override def insertMany(files: List[File]): Future[Int] = {
        wrapper.getCollection(database,collection).transformWith({
            case Success(col) => {
                col.insert.many[File](files).transformWith({
                    case Success(result) => {
                        if (result.ok) {
                            Future.successful(result.nModified)
                        } else {
                            throw new Exception(result.errmsg.get)
                        }
                    }
                    case Failure(cause) => throw cause
                })
            }
            case Failure(cause) => throw cause
        })
    }

    override def update(file: File): Future[Option[File]] = {
        wrapper.getCollection(database,collection).transformWith({
            case Success(col) => {
                col.findAndUpdate[BSONDocument, File](BSONDocument("_id" -> file.id), file, upsert = true)
                .map(_.result[File])
            }
            case Failure(cause) => throw cause
        })
    }

    override def updateMany(files: List[File]): Future[Int] = {
        wrapper.getCollection(database, collection).transformWith({
            case Success(col) => {
                val updateBuilder = col.update(ordered = true)
                val updates = Seq[Future[col.UpdateElement]]()
                files.foreach(file => {
                    updates :+ updateBuilder.element[BSONDocument, File](
                        q = BSONDocument("_id" -> file.id),
                        u = file,
                        upsert = true,
                        multi = false
                    )
                })
                val bulkUpdateResult = Future.sequence(updates).flatMap { ops => updateBuilder.many(ops) }
                bulkUpdateResult.transformWith({
                    case Success(result) => {
                        if (result.ok) {
                            Future.successful(result.nModified)
                        } else {
                            throw new Exception(result.errmsg.get)
                        }
                    }
                    case Failure(cause) => throw cause
                })
            }
            case Failure(cause) => throw cause
        })
    }

    override def delete(file: File): Future[Option[File]] = {
        wrapper.getCollection(database,collection).transformWith({
            case Success(col) => {
                col.findAndRemove[BSONDocument](BSONDocument("_id" -> file.id))
                .map(_.result[File])
            }
            case Failure(exception) => throw new Exception(exception) 
        })
    }

    override def deleteMany(files: List[File]): Future[Int] = {
        wrapper.getCollection(database,collection).transformWith({
            case Success(col) => {
                val deleteBuilder = col.delete(ordered = false)
                val deletes: Seq[Future[col.DeleteElement]] = files.map(file => {
                    deleteBuilder.element[BSONDocument, File](
                        q = BSONDocument("_id" -> file.id),
                        limit = None,
                        collation = None
                    )
                })
                
                val bulkDeleteResult = Future.sequence(deletes).flatMap{ ops => deleteBuilder.many(ops) }
                bulkDeleteResult.transformWith({
                    case Success(result) => {
                        if(result.ok) {
                            Future.successful(result.n)
                        }else{
                            throw new Exception(result.errmsg.get)
                        }
                    } 
                    case Failure(cause) => throw cause
                })
            }
            case Failure(cause) => throw cause
        })
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

    def findByID(id: String): Future[File] = {
        wrapper.getCollection(database, collection).transformWith({
            case Success(col) => {
                aggregate(pipeline = GetFileFromId.pipeline(col).setId(id)).transformWith({
                    case Success(files) => Future.successful(files(0))
                    case Failure(cause) => throw new Exception(cause)
                })
            }
            case Failure(cause) => throw new Exception(cause)
        })
    }

    def findByPath(path: String): Future[File] = {
        wrapper.getCollection(database, collection).transformWith({
            case Success(col) => {
                aggregate(pipeline = GetFileFromPath.pipeline(col).setPath(path)).transformWith({
                    case Success(files) => {
                        if (files.length == 0) throw new Exception("Couldn't find file with specified path")
                        Future.successful(files(0))
                    }
                    case Failure(cause) => throw new Exception(cause)
                })
            }
            case Failure(cause) => throw new Exception(cause)
        })  
    }

    def findMany(ids: List[String]): Future[List[File]] = {
        wrapper.getCollection(database, collection).transformWith({
            case Success(col) => {
                val pipeline = GetFilesFromId.pipeline(col)
                ids.foreach(id => {
                    pipeline.addId(id)
                })
                aggregate(pipeline = pipeline).transformWith({
                    case Success(files) => {
                        Future.successful(files)
                    }
                    case Failure(cause) => throw new Exception(cause)
                })
            }
            case Failure(cause) => throw cause
        })
    }

    def getFileChildren(dir: File): Future[List[File]] = {
        wrapper.getCollection(database, collection).transformWith({
            case Success(col) => {
                dir.path match {
                    case Some(path: String) => {
                        aggregate(pipeline = GetFileChildrenFromId.pipeline(col).setId(dir.id.stringify).setPathPrefix(path))
                        .transformWith({
                            case Success(files) => {
                                Future.successful(files)
                            }
                            case Failure(cause) => throw cause
                        })
                    }
                    case None => throw new Exception("Couldn't parse value 'path' from File object")
                }
            }
            case Failure(cause) => throw cause
        })
    }
}
