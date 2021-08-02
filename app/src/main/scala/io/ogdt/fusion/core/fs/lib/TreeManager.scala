package io.ogdt.fusion.core.fs.lib

import io.ogdt.fusion.core.db.wrappers.mongo.ReactiveMongoWrapper
import io.ogdt.fusion.core.db.models.documents.File
import io.ogdt.fusion.core.db.datastores.documents.FileStore

import reactivemongo.api.bson.BSONObjectID

import scala.util.Success
import scala.util.Failure
import scala.concurrent.Future

import io.ogdt.fusion.core.db.datastores.documents.exceptions.FileNotFoundException
import scala.concurrent.ExecutionContext

// DEBUG
import org.slf4j.Logger
import org.slf4j.LoggerFactory
// end-DEBUG

object TreeManager {

    def createFile(file: File)(implicit wrapper: ReactiveMongoWrapper, ec: ExecutionContext): Future[Unit] = {
        file.path match {
            case Some(path: String) => {
                getFileFromPath(path).transformWith({
                    case Success(existingFile) => Future.failed(new Exception("File already exists")) // TODO : changer pour une custom
                    case Failure(cause) => {
                        cause match {
                            case e: FileNotFoundException => {
                                getFileFromPath(path.replaceAll("/"+file.name, "")).transformWith({
                                    case Success(dir) => {
                                        if (!dir.isDirectory) Future.failed(new Exception(s"${dir.path.get} is not a directory")) // TODO : changer pour une custom
                                        val fileToInsert: File = file.copy(parent = Some(dir.id))
                                        new FileStore()
                                        .insert(fileToInsert).transformWith({
                                            case Success(result) => Future.unit
                                            case Failure(cause) => Future.failed(new Exception("Cannot insert file", cause)) // TODO : changer pour une custom
                                        })
                                    }
                                    case Failure(cause) => Future.failed(cause)
                                })
                            }
                            case e: Throwable => Future.failed(new Exception("Unhandled error", e)) // TODO : changer pour une custom
                            case _ => Future.failed(new Exception("Unknown error")) // TODO : changer pour une custom
                        }
                    }
                })
            }
            case None => Future.failed(new Exception("Couldn't parse value 'path' from File object")) // TODO : changer pour une custom
        }
    }
        

    def getFileFromId(id: String)(implicit wrapper: ReactiveMongoWrapper, ec: ExecutionContext): Future[File] = {
        new FileStore()
        .findByID(id).transformWith({
            case Success(file) => Future.successful(file)
            case Failure(cause) => Future.failed(cause)
        })
    }

    def getFileFromPath(path: String)(implicit wrapper: ReactiveMongoWrapper, ec: ExecutionContext): Future[File] = {
        new FileStore()
        .findByPath(path).transformWith({
            case Success(file) => Future.successful(file)
            case Failure(cause) => Future.failed(cause)
        })
    }

    def getManyFiles(ids: List[String])(implicit wrapper: ReactiveMongoWrapper, ec: ExecutionContext): Future[List[File]] = {
        new FileStore()
        .findMany(ids).transformWith({
            case Success(files) => {
                Future.successful(files)
            }
            case Failure(cause) => Future.failed(new Exception("bla bla bla",cause)) // TODO : changer pour une custom
        })
    }

    def getChildrenOf(dir: File)(implicit wrapper: ReactiveMongoWrapper, ec: ExecutionContext): Future[List[File]] = {
        new FileStore()
        .getFileChildren(dir).transformWith({
            case Success(files) => Future.successful(files)
            case Failure(cause) => Future.failed(new Exception("bla bla bla",cause)) // TODO : changer pour une custom
        })
    }

    def getParentOf(file: File)(implicit wrapper: ReactiveMongoWrapper, ec: ExecutionContext): Future[File] = {
        file.path match {
            case Some(path: String) => {
                getFileFromPath(path.replaceAll("/"+file.name,""))
            }
            case None => Future.failed(new Exception("Couldn't parse value 'path' from File object")) // TODO : changer pour une custom
        }
    }
    
    def updateFile(file: File)(implicit wrapper: ReactiveMongoWrapper, ec: ExecutionContext): Future[File] = {
        new FileStore()
        .update(file).transformWith({
            case Success(file) => {
                file match {
                    case Some(matchingfile: File) => Future.successful(matchingfile)
                    case None => Future.failed(new Exception("file not found")) // TODO : changer pour une custom
                }
            }
            case Failure(cause) =>  Future.failed(new Exception("bla bla bla",cause)) // TODO : changer pour une custom
        })
    }

    def deleteFile(file: File)(implicit wrapper: ReactiveMongoWrapper, ec: ExecutionContext): Future[File] = {
        getChildrenOf(file).transformWith({
            case Success(children) => {
                // Handle case where file to delete has child
                if (children.length > 0) Future.failed(new Exception("Directory can't be deleted if not empty")) // TODO : changer pour une custom
                else
                new FileStore()
                .delete(file).transformWith({
                    case Success(deletedFile) => {
                        deletedFile match {
                            case Some(matchingfile: File) => Future.successful(matchingfile)
                            case None => Future.failed(new Exception("file not found")) // TODO : changer pour une custom
                        }
                    }
                    case Failure(cause) => Future.failed(new Exception("bla bla bla",cause)) // TODO : changer pour une custom
                })
            }
            case Failure(cause) => Future.failed(new Exception("bla bla bla",cause)) // TODO : changer pour une custom
        })
    }

    final case class DeleteManyFilesResult(deleted: Int, errors: List[String])

    def deleteManyFiles(files: List[File])(implicit wrapper: ReactiveMongoWrapper, ec: ExecutionContext): Future[DeleteManyFilesResult] = {

        Future.sequence(
            files.map(file => {
                getChildrenOf(file).map(children => children.length > 0)
            })
        ).map(lookup => (files zip lookup))
        .transformWith(lookup => {
            val validDelete: List[File] = lookup.get.filter(_._2 == false).map(_._1)
            val childLookupError: List[String] = lookup.get.filter(_._2 == true).map("Couldn't delete directory "+_._1.path.get+" as it has still children")

            new FileStore()
            .deleteMany(validDelete).transformWith({
                case Success(deletedFilesCount) => {
                    Future.successful(DeleteManyFilesResult(deletedFilesCount, childLookupError))
                }
                case Failure(cause) => Future.failed(new Exception("bla bla bla",cause)) // TODO : changer pour une custom
            })
        })
    }
}
