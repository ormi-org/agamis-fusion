package io.agamis.fusion.external.api.rest.actors

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors

import scala.concurrent.ExecutionContext
import io.agamis.fusion.core.db.wrappers.mongo.ReactiveMongoWrapper
import io.agamis.fusion.external.api.rest.dto.fs.file.FileDto

object FileRepository {
  sealed trait Status

  object Successful extends Status

  object Failed extends Status

  sealed trait Response

  case object OK extends Response

  final case class KO(reason: String) extends Response

  sealed trait Command

  final case class AddFile(file: FileDto, replyTo: ActorRef[Response]) extends Command

  final case class GetFileById(id: String, replyTo: ActorRef[FileDto]) extends Command

  final case class GetFileByPath(path: String, replyTo: ActorRef[FileDto]) extends Command

  final case class DeleteFile(fileId: String, replyTo: ActorRef[Response]) extends Command

  final case class DeleteManyFile(files: List[FileDto], replyTo: ActorRef[Response]) extends Command

  final case class UpdateFile(file: FileDto, replyTo: ActorRef[Response]) extends Command

  def apply()(implicit system: ActorSystem[_]): Behavior[Command] =

    Behaviors.setup { context =>

      implicit val ec: ExecutionContext = context.executionContext
      implicit val mongoWrapper: ReactiveMongoWrapper = ReactiveMongoWrapper(system)

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
        case DeleteFile(fileId, replyTo) =>
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