package io.ogdt.fusion.core.fs.actors

import akka.Done
import akka.actor.typed.{Behavior, Signal, PostStop, ActorRef}
import akka.actor.typed.scaladsl.{ActorContext, AbstractBehavior, Behaviors}

import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import io.ogdt.fusion.core.db.wrappers.mongo.ReactiveMongoWrapper
import io.ogdt.fusion.core.db.models.sql.User
import io.ogdt.fusion.core.db.datastores.sql.UserStore
import io.ogdt.fusion.core.fs.lib.TreeManager

// temp
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.bson.BSONDateTime
import reactivemongo.api.bson.BSONObjectID
import java.util.UUID
// ---end temp

import org.apache.ignite.IgniteException

import scala.util.Success
import scala.util.Failure
import scala.collection.parallel.mutable.ParArray
import scala.collection.immutable.ArraySeq
import scala.collection.parallel.immutable.ParVector

import scala.concurrent.ExecutionContext.Implicits.global
import java.time.Instant

import io.ogdt.fusion.core.db.datastores.models.documents.File

// DEBUG
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import io.ogdt.fusion.core.db.models.documents.nested.file.Metadata
import io.ogdt.fusion.core.db.models.documents.nested.file.metadata.FusionXmlMeta
import io.ogdt.fusion.core.db.models.documents.nested.file.Acl
// end-DEBUG

object FusionFS {

    sealed trait Command
    case object GracefulShutdown extends Command

    def apply(): Behavior[FusionFS.Command] = {
        Behaviors.setup[FusionFS.Command](context => new FusionFS(context))
    }
}

class FusionFS(context: ActorContext[FusionFS.Command]) extends AbstractBehavior[FusionFS.Command](context) {
    import FusionFS._

    // implicit val igniteWrapper = IgniteClientNodeWrapper(context.system)
    implicit val mongoWrapper = ReactiveMongoWrapper(context.system)

    context.setLoggerName("io.ogdt.fusion.fs")

    context.log.info("FusionFS Application started")

    // DEBUG
    var logger: Logger = LoggerFactory.getLogger(getClass());
    // end-DEBUG

    TreeManager.getManyFiles(List(
        "606f4492130000130044e695",
        "606f4492130000130044e696",
        "607dc2a51b00001b00ae5454"
    ))
    .onComplete({
        case Success(files: List[File]) => {
            TreeManager.deleteManyFiles(files).onComplete({
                case Success(deleteResult) => {
                    logger.info("Successfuly deleted " + deleteResult.deleted + " files")
                    deleteResult.errors.foreach(error => {
                        logger.info(error)
                    })
                }
                case Failure(cause) => throw cause
            })
            // TreeManager.getChildrenOf(file).onComplete({
            //     case Success(files) => logger.info(files.toString())
            //     case Failure(cause) => throw cause
            // })
        }
        case Failure(cause) => throw cause
    })

    // TreeManager.getFileFromPath("/rootdir")
    // .onComplete({
    //     case Success(file: File) => {
    //         logger.info(file.toString())
    //     }
    //     case Failure(cause) => throw cause
    // })

    // TreeManager.createFile(
    //     new File(
    //         id = BSONObjectID.generate(),
    //         name = "testFile.oproto",
    //         `type` = File.FILE,
    //         path = Some("/rootdir/testFile.oproto"),
    //         parent = None,
    //         chunkList = None,
    //         metadata = new Metadata(
    //             size = None,
    //             creationDate = Instant.now(),
    //             lastVersionDate = None,
    //             lastModificationDate = Instant.now(),
    //             versionsCount = None,
    //             chainsCount = None,
    //             fusionXML = Some(new FusionXmlMeta(
    //                 xmlSchemaFileId = UUID.randomUUID(),
    //                 originAppId = "io.ogdt.apps.official:test"
    //             )),
    //             hidden = false,
    //             readonly = false
    //         ),
    //         versioned = Some(true),
    //         acl = new Acl(
    //             userAccess = List(

    //             ),
    //             groupAccess = None
    //         ),
    //         owner = UUID.fromString("7f8e863a-49db-46a8-9a57-d38b4f51e60c")
    //     )
    // ).onComplete({
    //     case Success(result) => {
    //         if (result) logger.info("Successfuly created new file")
    //         else logger.info("Failed to create new file")
    //     }
    //     case Failure(cause) => throw cause
    // })

    override def onMessage(msg: Command): Behavior[Command] = {
        msg match {
            case GracefulShutdown =>
                context.log.info("Received graceful shutdown command...")
                Behaviors.stopped
        }
    }

    override def onSignal: PartialFunction[Signal,Behavior[FusionFS.Command]] = {
        case PostStop =>
            context.log.info("FusionFS Application stopped")
            context.system.terminate()
            this
    }
}