package io.ogdt.fusion.external.http.actors

import akka.pattern.pipe
import akka.actor.typed.{Behavior, Signal, PostStop,ActorSystem,  ActorRef}
import akka.actor.typed.scaladsl.{ActorContext, AbstractBehavior, Behaviors}
import reactivemongo.api.bson.BSONObjectID

import java.util.UUID
import java.time.Instant

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import scala.util.Success
import scala.util.Failure
import scala.util.Try

import io.ogdt.fusion.external.http.entities.nested.file.Metadata
import io.ogdt.fusion.external.http.entities.nested.file.Acl
import io.ogdt.fusion.external.http.entities.File
import io.ogdt.fusion.external.http.entities.nested.file.metadata.FusionXmlMeta

import io.ogdt.fusion.core.db.datastores.documents.FileStore
import io.ogdt.fusion.core.fs.lib.TreeManager
import io.ogdt.fusion.core.db.models.documents.{File => FileDocument}
import io.ogdt.fusion.core.db.wrappers.mongo.ReactiveMongoWrapper
import org.apache.ignite.internal.util.typedef.F
import io.ogdt.fusion.core.db.models.documents

object FileRepository {
    sealed trait Status
    object Successful extends Status
    object Failed extends Status

    sealed trait Response 
    case object OK extends Response 
    final case class KO(reason: String) extends Response 

    sealed trait Command

    final case class AddFile(file: File, replyTo: ActorRef[Response]) extends Command
    final case class GetFileById(id: String, replyTo: ActorRef[File]) extends Command
    final case class GetFileByPath(path: String, replyTo: ActorRef[File]) extends Command
    final case class DeleteFile(file: File, replyTo: ActorRef[Response]) extends Command
    final case class DeleteManyFile(files: List[File], replyTo: ActorRef[Response]) extends Command
    final case class UpdateFile(file: File, replyTo: ActorRef[Response]) extends Command
    
    def apply()(implicit system: ActorSystem[_]): Behavior[Command] = 

        Behaviors.setup { context =>

            implicit val ec: ExecutionContext = context.executionContext
            implicit val mongoWrapper = ReactiveMongoWrapper(system)

            Behaviors.receiveMessage {
                case AddFile(file, replyTo) => 
                    // TreeManager.createFile(file).onComplete({
                    //     case Success(file) => replyTo ! OK
                    //     case Failure(error) => replyTo ! KO(error.toString + (if (error.getCause() != null) " : " + error.getCause().toString else "")) // TODO : changer pour une custom
                    // })
                    Behaviors.same
                case GetFileById(id, replyTo) =>
                    // TreeManager.getFileFromId(id).onComplete({
                    //     case Success(file) => replyTo ! File(Some(file.id),
                    //                                             file.name,
                    //                                             file.`type` match {
                    //                                                 case FileDocument.DIRECTORY => File.DIRECTORY
                    //                                                 case FileDocument.FILE => File.FILE
                    //                                             },
                    //                                             file.path,
                    //                                             file.parent,
                    //                                             file.chunkList,
                    //                                             file.metadata, 
                    //                                             file.versioned,
                    //                                             file.acl,
                    //                                             file.owner)
                    //     case Failure(cause) => throw new Exception(cause)
                    // })
                    Behaviors.same
                case GetFileByPath(path, replyTo) => 
                    // TreeManager.getFileFromPath(path).onComplete({
                    //     case Success(file) => replyTo ! File(Some(file.id),
                    //                                             file.name,
                    //                                             file.`type` match {
                    //                                                 case FileDocument.DIRECTORY => File.DIRECTORY
                    //                                                 case FileDocument.FILE => File.FILE
                    //                                             },
                    //                                             file.path,
                    //                                             file.parent,
                    //                                             file.chunkList,
                    //                                             file.metadata, 
                    //                                             file.versioned,
                    //                                             file.acl,
                    //                                             file.owner)
                    //     case Failure(cause) => throw new Exception(cause)  
                    // })
                    Behaviors.same
                case UpdateFile(file, replyTo) => 
                    // TreeManager.updateFile(file).onComplete({
                    //     case Success(file) => replyTo ! OK
                    //     case Failure(error) => replyTo ! KO(error.toString + (if (error.getCause() != null) " : " + error.getCause().toString else "")) // TODO : changer pour une custom
                    // })
                    Behaviors.same
                case DeleteFile(file, replyTo) =>
                    // TreeManager.deleteFile(file).onComplete({
                    //     case Success(file) => replyTo ! OK
                    //     case Failure(error) => replyTo ! KO(error.toString + (if (error.getCause() != null) " : " + error.getCause().toString else "")) // TODO : changer pour une custom  
                    // })
                    Behaviors.same
                case DeleteManyFile(files, replyTo) => 
                    // delete many files
                    // TreeManager.deleteManyFiles(files).onComplete({
                    //     case Success(files) => replyTo ! OK
                    //     case Failure(error) => replyTo ! KO(error.toString + (if (error.getCause() != null) " : " + error.getCause().toString else "")) // TODO : changer pour une custom   
                    // })
                    replyTo ! KO("Not implemented yet")
                    Behaviors.same
            }
        }

}