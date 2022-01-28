package io.agamis.fusion.core.db.datastores.documents

import io.agamis.fusion.core.db.datastores.documents.aggregations.typed.Pipeline
import io.agamis.fusion.core.db.datastores.documents.aggregations.typed.file.{GetFileChildrenFromId, GetFileFromId, GetFileFromPath, GetFilesFromId}
import io.agamis.fusion.core.db.datastores.documents.exceptions.typed.file.{DuplicatedFileException, FileNotFoundException}
import io.agamis.fusion.core.db.datastores.typed.DocumentStore
import io.agamis.fusion.core.db.models.documents.file.File
import io.agamis.fusion.core.db.wrappers.mongo.ReactiveMongoWrapper
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.commands.WriteResult

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class FileStore(implicit wrapper: ReactiveMongoWrapper) extends DocumentStore[File] {

    override val database: String = "fusiondb"
    override val collection: String = "files"

    /** A method for inserting a new file in database
      *
      * [[File File]] is inserted
      *
      * @note This methods is Async and returns a [[scala.concurrent.Future Future]] which resolves a [[WriteResult WriteResult]]
      *
      * @param file the file to insert
      * @param ec   the '''implicit''' [[ExecutionContext ExecutionContext]] used to parallelize computing
      * @return a future [[WriteResult WriteResult]] which attests the acutal creation of the file
      */
    override def insert(file: File)(implicit ec: ExecutionContext): Future[WriteResult] = {
        wrapper.getCollection(database, collection).transformWith({
            case Success(col) =>
                col.insert.one[File](file).transformWith({
                    case Success(result) =>
                        Future.successful(result)
                    case Failure(cause) => throw cause
                })
            case Failure(cause) => throw cause
        })
    }

    /** A method for inserting several files in database
      *
      * [[File File]] is inserted
      *
      * @note This methods is Async and returns a [[scala.concurrent.Future Future]] which resolves a [[scala.Int Int]]
      *
      * @param files the files to insert
      * @param ec   the '''implicit''' [[ExecutionContext ExecutionContext]] used to parallelize computing
      * @return a future integer which attests the acutal creation of the file
      */
    override def insertMany(files: List[File])(implicit ec: ExecutionContext): Future[Int] = {
        wrapper.getCollection(database,collection).transformWith({
            case Success(col) =>
                col.insert.many[File](files).transformWith({
                    case Success(result) =>
                        if (result.ok) {
                            Future.successful(result.nModified)
                        } else {
                            throw new Exception(result.errmsg.get)
                        }
                    case Failure(cause) => throw cause
                })
            case Failure(cause) => throw cause
        })
    }

    /** A method for updating an existing file in database
      *
      * [[File File]] is inserted
      *
      * @note This methods is Async and returns a [[scala.concurrent.Future Future]] which resolves a [[WriteResult WriteResult]]
      *
      * @param file the file to update
      * @param ec   the '''implicit''' [[ExecutionContext ExecutionContext]] used to parallelize computing
      * @return a future [[Option Option]] of [[File File]] which reflect new file state in the database
      */
    override def update(file: File)(implicit ec: ExecutionContext): Future[Option[File]] = {
        wrapper.getCollection(database,collection).transformWith({
            case Success(col) =>
                col.findAndUpdate[BSONDocument, File](BSONDocument("_id" -> file.id), file, upsert = false)
                .map(_.result[File])
            case Failure(cause) => throw cause
        })
    }

    /** A method for updating an existing file in database
      *
      * [[File File]] is inserted
      *
      * @note This methods is Async and returns a [[scala.concurrent.Future Future]] which resolves a [[WriteResult WriteResult]]
      *
      * @param files a list of files to be updated
      * @param ec   the '''implicit''' [[ExecutionContext ExecutionContext]] used to parallelize computing
      * @return a future [[Option Option]] of [[File File]] which reflect new file state in the database
      */
    override def updateMany(files: List[File])(implicit ec: ExecutionContext): Future[List[File]] = {
        wrapper.getCollection(database, collection).transformWith({
            case Success(col) =>
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
                    case Success(result) =>
                        if (result.ok) {
                            Future.successful(result.upserted.asInstanceOf[Seq[File]].toList)
                        } else {
                            throw new Exception(result.errmsg.get)
                        }
                    case Failure(cause) => throw cause
                })
            case Failure(cause) => throw cause
        })
    }

    /** A method for deleting an existing file in database
      *
      * @param file the file to delete
      * @param ec   the '''implicit''' [[ExecutionContext ExecutionContext]] used to parallelize computing
      * @return a future [[Option Option]] of [[File File]] which reflects the file state that has been removed from database
      */
    override def delete(file: File)(implicit ec: ExecutionContext): Future[Option[File]] = {
        wrapper.getCollection(database,collection).transformWith({
            case Success(col) =>
                col.findAndRemove[BSONDocument](BSONDocument("_id" -> file.id))
                .map(_.result[File])
            case Failure(cause) => throw cause
        })
    }

    /** A method for deleting several existing files in database
      *
      * @param files the list of files to be deleted
      * @param ec    the '''implicit''' [[ExecutionContext ExecutionContext]] used to parallelize computing
      * @return a future [[Int Int]] which reflects the count of deleted files
      */
    override def deleteMany(files: List[File])(implicit ec: ExecutionContext): Future[Int] = {
        wrapper.getCollection(database,collection).transformWith({
            case Success(col) =>
                val deleteBuilder = col.delete(ordered = false)
                val deletes: Seq[Future[Any]] = files.map(file => {
                    deleteBuilder.element[BSONDocument, File](
                        q = BSONDocument("_id" -> file.id),
                        limit = None,
                        collation = None
                    )
                })

                val bulkDeleteResult = Future.sequence(deletes).flatMap{ ops => deleteBuilder.many(ops.asInstanceOf) }
                bulkDeleteResult.transformWith({
                    case Success(result) =>
                        if (result.ok) {
                            Future.successful(result.n)
                        } else {
                            throw new Exception(result.errmsg.get)
                        }
                    case Failure(cause) => throw cause
                })
            case Failure(cause) => throw cause
        })
    }

    /** A method for executing MongoDB aggregation
      *
      * @param pipeline the parametarized pipeline to execute
      * @param ec       the '''implicit''' [[ExecutionContext ExecutionContext]] used to parallelize computing
      * @return a future [[List List]] of [[File File]] which reflects the state of fetched files in database
      */
    override def aggregate(pipeline: Pipeline)(implicit ec: ExecutionContext): Future[List[File]] = {
        wrapper.getCollection(database, collection).transformWith({
            case Success(col) =>
                col.aggregatorContext[File](
                    pipeline = pipeline.get.asInstanceOf[List[col.AggregationFramework.PipelineOperator]]
                ).prepared.cursor.collect[List]()
            case Failure(cause) => throw cause
        })
    }

    /** A method for retrieving a specific file by its id
      *
      * @note based on aggregation, it calls aggregate(Pipeline)
      *
      * @param id the id of the file to fetch
      * @param ec the '''implicit''' [[ExecutionContext ExecutionContext]] used to parallelize computing
      * @return a future [[File File]] which reflects the state of fetched file in database
      */
    def findByID(id: String)(implicit ec: ExecutionContext): Future[File] = {
        wrapper.getCollection(database, collection).transformWith({
            case Success(col) =>
                aggregate(pipeline = GetFileFromId.pipeline(col).setId(id)).transformWith({
                    case Success(files) =>
                        files.length match {
                            case 0 => Future.failed(FileNotFoundException("Couldn't find file with specified id"))
                            case 1 => Future.successful(files.head)
                            case _ => Future.failed(DuplicatedFileException("File id duplication issue")) // TODO : changer pour une custom
                        }
                    case Failure(cause) => Future.failed(new Exception("bla bla bla",cause)) // TODO : changer pour une custom
                })
            case Failure(cause) => Future.failed(new Exception("bla bla bla",cause)) // TODO : changer pour une custom
        })
    }

    /** A method for retrieving a specific file by its path in the file tree
      *
      * @note based on aggregation, it calls aggregate(Pipeline)
      *
      * @param path the path of the file to fetch
      * @param ec the '''implicit''' [[ExecutionContext ExecutionContext]] used to parallelize computing
      * @return a future [[File File]] which reflects the state of fetched file in database
      */
    def findByPath(path: String)(implicit ec: ExecutionContext): Future[File] = {
        wrapper.getCollection(database, collection).transformWith({
            case Success(col) =>
                aggregate(pipeline = GetFileFromPath.pipeline(col).setPath(path)).transformWith({
                    case Success(files) =>
                        files.length match {
                            case 0 => Future.failed(FileNotFoundException("Couldn't find file with specified path"))
                            case 1 => Future.successful(files.head)
                            case _ => Future.failed(new Exception("File path duplication issue")) // TODO : changer pour une custom
                        }
                    case Failure(cause) => Future.failed(new Exception("findByPath pipeline execution failure", cause)) // TODO : changer pour une custom
                })
            case Failure(cause) => Future.failed(new Exception("Failed to get collection", cause)) // TODO : changer pour une custom
        })  
    }

    /** A method for retrieving specific files by their ids
      *
      * @note based on aggregation, it calls aggregate(Pipeline)
      *
      * @param ids the list of ids of files to fetch
      * @param ec the '''implicit''' [[ExecutionContext ExecutionContext]] used to parallelize computing
      * @return a future [[List List]] of [[File File]] which reflects the state of fetched files in database
      */
    def findMany(ids: List[String])(implicit ec: ExecutionContext): Future[List[File]] = {
        wrapper.getCollection(database, collection).transformWith({
            case Success(col) =>
                val pipeline = GetFilesFromId.pipeline(col)
                ids.foreach(id => {
                    pipeline.addId(id)
                })
                aggregate(pipeline = pipeline).transformWith({
                    case Success(files) =>
                        Future.successful(files)
                    case Failure(cause) => Future.failed(new Exception("bla bla bla",cause)) // TODO : changer pour une custom
                })
            case Failure(cause) => Future.failed(new Exception("bla bla bla",cause)) // TODO : changer pour une custom
        })
    }

    /** A method for retrieving specific children files by their parent ref
      *
      * @note based on aggregation, it calls aggregate(Pipeline)
      *
      * @param dir the directory to search file in
      * @param ec the '''implicit''' [[ExecutionContext ExecutionContext]] used to parallelize computing
      * @return a future [[List List]] of [[File File]] which reflects the state of fetched files in database
      */
    def getFileChildren(dir: File)(implicit ec: ExecutionContext): Future[List[File]] = {
        wrapper.getCollection(database, collection).transformWith({
            case Success(col) =>
                dir.path match {
                    case Some(path: String) =>
                        aggregate(pipeline = GetFileChildrenFromId.pipeline(col).setId(dir.id.stringify).setPathPrefix(path))
                        .transformWith({
                            case Success(files) =>
                                Future.successful(files)
                            case Failure(cause) => throw cause
                        })
                    case None => Future.failed(new Exception("Couldn't parse value 'path' from File object")) // TODO : changer pour une custom
                }
            case Failure(cause) => Future.failed(new Exception("bla bla bla", cause)) // TODO : changer pour une custom
        })
    }
}
