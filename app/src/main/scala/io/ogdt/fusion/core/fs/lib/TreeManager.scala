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

object TreeManager {

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

    def deleteManyFiles(files: List[File])(implicit wrapper: ReactiveMongoWrapper): Future[Int] = {
        new FileStore()
        .deleteMany(files).transformWith({
            case Success(deletedFilesCount) => {
                Future.successful(deletedFilesCount)
            }
            case Failure(cause) => throw cause
        })
    }
}
