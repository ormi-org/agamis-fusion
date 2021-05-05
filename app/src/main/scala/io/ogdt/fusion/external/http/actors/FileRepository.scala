package io.ogdt.fusion.external.http.actors

import akka.actor.typed.{Behavior, Signal, PostStop, ActorRef}
import akka.actor.typed.scaladsl.{ActorContext, AbstractBehavior, Behaviors}
import reactivemongo.api.bson.BSONObjectID

import java.util.UUID
import java.time.Instant

import scala.concurrent.ExecutionContext
import scala.util.Success
import scala.util.Failure
import scala.util.Try

import io.ogdt.fusion.external.http.entities.nested.file.Metadata
import io.ogdt.fusion.external.http.entities.nested.file.Acl
import io.ogdt.fusion.external.http.entities.File
import io.ogdt.fusion.external.http.entities.nested.file.metadata.FusionXmlMeta

import akka.actor.typed.ActorSystem
import akka.pattern.pipe

import io.ogdt.fusion.core.db.datastores.documents.FileStore
import io.ogdt.fusion.core.fs.lib.TreeManager
// import io.ogdt.fusion.core.db.models.documents.{File => FileDocument}
import io.ogdt.fusion.core.db.wrappers.mongo.ReactiveMongoWrapper
import scala.concurrent.Future
import scala.concurrent.Await

object FileRepository {
    sealed trait Status
    object Successful extends Status
    object Failed extends Status

    sealed trait Response 
    case object OK extends Response
    final case class KO(reason: String) extends Response

    sealed trait Command
    final case class AddInitFile(file: File, replyTo: ActorRef[File]) extends Command
    final case class GetFileById(id: BSONObjectID, replyTo: ActorRef[Option[File]]) extends Command
    final case class AddFile(file: File, replyTo: ActorRef[Response]) extends Command
    final case class ClearFiles(replyTo: ActorRef[Response]) extends Command
    
    def apply()(implicit system: ActorSystem[_]): Behavior[Command] = 

        Behaviors.setup { context =>

            implicit val ec: ExecutionContext = context.executionContext
            implicit val mongoWrapper = ReactiveMongoWrapper(system)

            Behaviors.receiveMessage {
                case AddFile(file, replyTo) => 
                    // if (file == null) {
                    //     replyTo ! KO("File is null")
                    //      Behaviors.same
                    // } else {
                    //     replyTo ! OK 
                    //     Behaviors.same
                        // context.pipeToSelf(TreeManager.createFile(file).transformWith({
                        //     case Success(value) => value
                        //     case Failure(cause) => cause
                        // }))
                    // }
                    // TreeManager.createFile(file).onComplete({
                    //     case Success(result) => replyTo ! result
                    //     case Failure(cause) => throw cause
                    // })
                    replyTo ! OK
                    Behaviors.same
                case AddInitFile(file, replyTo) =>
                    val file: File = new File(
                                    id = Some(BSONObjectID.generate()),
                                    name = "rootdir1",
                                    `type` = File.DIRECTORY,
                                    path = Some("/id"),
                                    parent = None,
                                    chunkList = None,
                                    metadata = new Metadata(
                                        size = None,
                                        creationDate = Instant.now(),
                                        lastVersionDate = None,
                                        lastModificationDate = Instant.now(),
                                        versionsCount = None,
                                        chainsCount = None,
                                        fusionXML = Some(new FusionXmlMeta(
                                            xmlSchemaFileId = UUID.randomUUID(),
                                            originAppId = "io.ogdt.apps.official:test"
                                        )),
                                        hidden = false,
                                        readonly = false
                                    ),
                                    versioned = Some(true),
                                    acl = new Acl(
                                        userAccess = List(

                                        ),
                                        groupAccess = None
                                    ),
                                    owner = UUID.fromString("7f8e863a-49db-46a8-9a57-d38b4f51e60c")
                                )
                    replyTo ! file
                    Behaviors.same
                case GetFileById(id, replyTo) =>
                    replyTo ! Some(File(
                        id = Some(BSONObjectID.generate()),
                        name = "testFile.oproto",
                        `type` = File.FILE,
                        path = Some("/rootdir/testFile.oproto"),
                        parent = None,
                        chunkList = None,
                        metadata = new Metadata(
                            size = None,
                            creationDate = Instant.now(),
                            lastVersionDate = None,
                            lastModificationDate = Instant.now(),
                            versionsCount = None,
                            chainsCount = None,
                            fusionXML = Some(new FusionXmlMeta(
                                xmlSchemaFileId = UUID.randomUUID(),
                                originAppId = "io.ogdt.apps.official:test"
                            )),
                            hidden = false,
                            readonly = false
                        ),
                        versioned = Some(true),
                        acl = new Acl(
                            userAccess = List(

                            ),
                            groupAccess = None
                        ),
                        owner = UUID.fromString("7f8e863a-49db-46a8-9a57-d38b4f51e60c")
                    ))
                    // replyTo ! files.get(id)
                    Behaviors.same
                case ClearFiles(replyTo) =>
                    replyTo ! OK
                    Behaviors.same
                    // FileRepository(Map.empty)
            }
        }

}