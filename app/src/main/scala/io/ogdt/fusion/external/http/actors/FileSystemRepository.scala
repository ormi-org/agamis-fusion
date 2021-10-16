package io.ogdt.fusion.external.http.actors

import java.util.UUID
import java.time.Instant
import java.sql.Timestamp

import scala.util.Success
import scala.util.Failure
import scala.concurrent.Future

import akka.actor.typed.Behavior
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import scala.concurrent.ExecutionContext

import reactivemongo.api.bson.BSONObjectID

import io.ogdt.fusion.core.db.wrappers.mongo.ReactiveMongoWrapper

import io.ogdt.fusion.core.db.datastores.documents.exceptions.FileNotFoundException

import akka.actor.typed.ActorRef

import io.ogdt.fusion.external.http.entities.File
import io.ogdt.fusion.external.http.entities.nested.file.{Metadata, Acl}
import io.ogdt.fusion.external.http.entities.nested.file.metadata.FusionXmlMeta

import io.ogdt.fusion.core.fs.lib.TreeManager
import io.ogdt.fusion.core.db.datastores.documents.FileStore
import io.ogdt.fusion.core.db.models.documents.{File => FileDocument}
import io.ogdt.fusion.core.db.models.documents

import io.ogdt.fusion.external.http.entities.request.FileSystemInitRequest

object FileSystemRepository {

    sealed trait Status
    object Successful extends Status
    object Failed extends Status

    sealed trait Response 
    case object OK extends Response 
    final case class KO(reason: String) extends Response 

    final case class InitFileSystem(fsRequest: FileSystemInitRequest, replyTo: ActorRef[Response]) extends Command

    sealed trait Command

    
    def apply()(implicit system: ActorSystem[_]): Behavior[Command] = 

        Behaviors.setup { context =>

            implicit val ec: ExecutionContext = context.executionContext
            implicit val mongoWrapper = ReactiveMongoWrapper(system)

            Behaviors.receiveMessage {
            case InitFileSystem(fsRequest,replyTo) => // AddRootDir ? / Sinon juste InitFileSystem et tu fais toutes les opérations nécessaires dedans ?
                val newFSId: UUID = UUID.randomUUID()
                val newFileId: BSONObjectID = BSONObjectID.generate()
                val rootdir = new FileDocument(
                    newFileId,
                    newFSId.toString,
                    FileDocument.DIRECTORY,
                    None,
                    None,
                    None,
                    new Metadata(
                        Some(0),
                        Instant.now(),
                        None,
                        Instant.now(),
                        None,
                        None,
                        None,
                        false,
                        false
                    ),
                    Some(false),
                    new documents.nested.file.Acl(
                        List(), // Ajouter l'utilisateur créateur et l'administrateur avec tous les droits
                        Some(List())
                    ),
                    UUID.randomUUID() // Set à l'id de l'utilisateur créateur
                )
                // new FileStore().findByPath("/"+rootdir.id.toString).transformWith({
                //     case Success(existingFile) => Future.failed(new Exception("Rootdir for this new FileSystem already exists"))
                //     case Failure(cause) => cause match {
                //         case _: FileNotFoundException => TreeManager.createFile(rootdir).onComplete({
                //                 case Success(file) => replyTo ! OK
                //                 case Failure(cause) => throw new Exception(cause)  
                //         }) 
                //         case _ => Future.failed(new Exception("Failed to reference new filesystem", cause))
                //     }
                // })
                new FileStore().insert(rootdir).onComplete({
                            case Success(file) => replyTo ! OK
                            case Failure(cause) => throw new Exception(cause)                      
                })
                Behaviors.same
            }
        }
}