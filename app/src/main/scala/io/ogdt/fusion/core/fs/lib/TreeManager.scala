package io.ogdt.fusion.core.fs.lib

import io.ogdt.fusion.core.db.wrappers.mongo.ReactiveMongoWrapper
import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper

import io.ogdt.fusion.core.db.datastores.documents.FileStore
import io.ogdt.fusion.core.db.datastores.models.documents.File

import reactivemongo.api.bson.BSONObjectID

import scala.util.Success
import scala.util.Failure
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import io.ogdt.fusion.core.db.datastores.caches.CachedFileStore

// DEBUG
import org.slf4j.Logger
import org.slf4j.LoggerFactory
// end-DEBUG

object TreeManager {

    // DEBUG
    var logger: Logger = LoggerFactory.getLogger(getClass());
    // end-DEBUG

    def createFile(file: File)(implicit wrapper: ReactiveMongoWrapper): Future[Boolean] = {
        file.path match {
            case Some(path: String) => {
                getFileFromPath(path.replaceAll("/"+file.name, "")).transformWith({
                    case Success(dir) => {
                        val fileToInsert: File = file.copy(parent = Some(dir.id))
                        new FileStore()
                        .insert(fileToInsert).transformWith({
                            case Success(result) => Future.successful(true)
                            case Failure(cause) => Future.successful(false)
                        })
                    }
                    case Failure(cause) => throw cause
                })
            }
            case None => throw new Exception("Couldn't parse value 'path' from File object")
        }
    }

    def getFileFromId(id: String)(implicit wrapper: ReactiveMongoWrapper, igniteWrapper: IgniteClientNodeWrapper): Future[File] = {
        new FileStore()
        .findByID(id).transformWith({
            case Success(file) => {
                new CachedFileStore()
                .put(file)
                Future.successful(file)
            }
            case Failure(cause) => throw cause
        })
    }

    def getFileFromPath(path: String)(implicit wrapper: ReactiveMongoWrapper): Future[File] = {
        new FileStore()
        .findByPath(path).transformWith({
            case Success(file) => Future.successful(file)
            case Failure(cause) => throw cause  
        })
    }

    def getManyFiles(ids: List[String])(implicit wrapper: ReactiveMongoWrapper): Future[List[File]] = {
        new FileStore()
        .findMany(ids).transformWith({
            case Success(files) => {
                Future.successful(files)
            }
            case Failure(cause) => throw cause
        })
    }

    def getChildrenOf(dir: File)(implicit wrapper: ReactiveMongoWrapper): Future[List[File]] = {
        new FileStore()
        .getFileChildren(dir).transformWith({
            case Success(files) => Future.successful(files)
            case Failure(cause) => throw cause
        })
    }

    def getParentOf(file: File)(implicit wrapper: ReactiveMongoWrapper): Future[File] = {
        file.path match {
            case Some(path: String) => {
                getFileFromPath(path.replaceAll("/"+file.name,""))
            }
            case None => throw new Exception("Couldn't parse value 'path' from File object")
        }
    }
    
    def updateFile(file: File)(implicit wrapper: ReactiveMongoWrapper): Future[File] = {
        new FileStore()
        .update(file).transformWith({
            case Success(file) => {
                file match {
                    case Some(matchingfile: File) => Future.successful(matchingfile)
                    case None => throw new Exception("file not found")
                }
            }
            case Failure(cause) =>  throw cause
        })
    }

    def deleteFile(file: File)(implicit wrapper: ReactiveMongoWrapper): Future[File] = {
        getChildrenOf(file).transformWith({
            case Success(children) => {
                // Handle case where file to delete has child
                if (children.length > 0) throw new Exception("Directory can't be deleted if not empty")
                new FileStore()
                .delete(file).transformWith({
                    case Success(deletedFile) => {
                        deletedFile match {
                            case Some(matchingfile: File) => Future.successful(matchingfile)
                            case None => throw new Exception("file not found")
                        }
                    }
                    case Failure(cause) => throw cause
                })
            }
            case Failure(cause) => throw cause
        })
    }

    final case class DeleteManyFilesResult(deleted: Int, errors: List[String])

    def deleteManyFiles(files: List[File])(implicit wrapper: ReactiveMongoWrapper): Future[DeleteManyFilesResult] = {

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
                case Failure(cause) => throw cause
            })
        })
    }
}
