package io.ogdt.fusion.core.fs.actors

import akka.Done
import akka.actor.typed.{Behavior, Signal, PostStop, ActorRef}
import akka.actor.typed.scaladsl.{ActorContext, AbstractBehavior, Behaviors}

import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import io.ogdt.fusion.core.db.wrappers.mongo.ReactiveMongoWrapper
import io.ogdt.fusion.core.db.models.sql.User
import io.ogdt.fusion.core.db.datastores.sql.UserStore


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

object FusionFS {

    sealed trait Command
    case object GracefulShutdown extends Command

    def apply(): Behavior[FusionFS.Command] = {
        Behaviors.setup[FusionFS.Command](context => new FusionFS(context))
    }
}

class FusionFS(context: ActorContext[FusionFS.Command]) extends AbstractBehavior[FusionFS.Command](context) {
    import FusionFS._

    // val igniteWrapper = IgniteClientNodeWrapper(context.system)
    val mongoWrapper = ReactiveMongoWrapper(context.system)

    context.setLoggerName("io.ogdt.fusion.fs")

    context.log.info("FusionFS Application started")

    // val rootdirUuid = BSONObjectID.generate()

    // val rootdir = BSONDocument(
    //     "_id" -> rootdirUuid,
    //     "name" -> "rootdir",
    //     "type" -> "DIRECTORY",
    //     "metadata" -> BSONDocument(
    //         "lastModificationDate" -> BSONDateTime(Instant.parse("2021-04-08T19:51:30Z").toEpochMilli()),
    //         "hidden" -> false,
    //         "readonly" -> false
    //     ),
    //     "ACL" -> BSONDocument(
    //         "userAccess" -> List(
    //             BSONDocument(
    //                 "userId" -> "7f8e863a-49db-46a8-9a57-d38b4f51e60c",
    //                 "rights" -> BSONDocument(
    //                     "read" -> true,
    //                     "read and execute" -> true,
    //                     "write" -> true
    //                 )
    //             )
    //         )
    //     ),
    //     "Owner" -> "7f8e863a-49db-46a8-9a57-d38b4f51e60c"
    // )

    // val file1 = BSONDocument(
    //     "_id" -> BSONObjectID.generate(),
    //     "name" -> "file1",
    //     "type" -> "FILE",
    //     "parent" -> rootdirUuid,
    //     "chunkList" -> List(
    //         "abdd12ee-3f9b-4ac4-999c-f05accb1b3d7",
    //         "b14ec0ad-1cc0-4467-9196-d09ab43f62fc",
    //         "dfe6590e-4dde-4f71-b7d6-3a88801707c7"
    //     ),
    //     "metadata" -> BSONDocument(
    //         "size" -> 0,
    //         "lastVersionDate" -> BSONDateTime(Instant.parse("2021-04-08T19:51:30Z").toEpochMilli()),
    //         "lastModificationDate" -> BSONDateTime(Instant.parse("2021-04-08T19:51:30Z").toEpochMilli()),
    //         "chainsCount" -> 0,
    //         "versionsCount" -> 0,
    //         "fusionXML" -> BSONDocument(
    //             "xmlSchemaFileId" -> "158b56dc-a79a-468f-8247-07319e6851df"
    //         ),
    //         "hidden" -> false,
    //         "readonly" -> false
    //     ),
    //     "versioned" -> true,
    //     "ACL" -> BSONDocument(
    //         "userAccess" -> List(
    //             BSONDocument(
    //                 "userId" -> "7f8e863a-49db-46a8-9a57-d38b4f51e60c",
    //                 "rights" -> BSONDocument(
    //                     "read" -> true,
    //                     "read and execute" -> true,
    //                     "write" -> true,
    //                     "versioning" -> true,
    //                     "advancedVersioning" -> true,
    //                     "aclManagement" -> true,
    //                     "advancedAclManagement" -> true,
    //                     "totalControl" -> true
    //                 )
    //             )
    //         )
    //     ),
    //     "Owner" -> "7f8e863a-49db-46a8-9a57-d38b4f51e60c"
    // )

    // mongoWrapper.getCollection("fusiondb", "files").onComplete({
    //     case Success(collection) => {
    //         collection.insert.one(BSONDocument("_id" -> BSONObjectID.generate(),"test"->"test"))
    //     }
    //     case Failure(exception) => {

    //     }
    // })

    // mongoWrapper.getCollection("fusiondb", "files").onComplete({
    //     case Success(collection) => {
    //         collection.insert.many(Seq(rootdir, file1)).onComplete({
    //             case Success(result) => {
    //                 if (result.ok) mongoWrapper.getLogger().info(s"Inserted ${result.n} documents")
    //             }
    //             case Failure(exception) => {

    //             }
    //         })
    //     }
    //     case Failure(exception) => {

    //     }
    // })

    // val userStore = UserStore.fromWrapper(igniteWrapper)
    // userStore.getUsers(new Array[String](0))
    // userStore.makeUser().setId(10).setFirstname("Daniel").setLastname("Copperfield").persist()

    // userStore.bulkPersistUsers(Vector(
    //     userStore.makeUser().setId(11).setFirstname("Daniel").setLastname("ForSureCopperfield"),
    //     userStore.makeUser().setId(12).setFirstname("Daniel").setLastname("NotCopperfield")
    // ))

    // try {
    //     val query = BSONDocument("_id" -> BSONObjectID.parse("60701d763300002f00a0f366").get)
    //     mongoWrapper.getCollection("fusiondb", "files").onComplete({
    //         case Success(collection) => {
    //             collection.find(query).one[BSONDocument].onComplete({
    //                 case Success(doc) => {
    //                     mongoWrapper.getLogger().info(BSONDocument.pretty(doc.get).toString())
    //                 }
    //                 case Failure(exception) => {
    //                     mongoWrapper.getLogger().info(exception.toString())
    //                 }
    //             })
    //         }
    //         case Failure(exception) => {
    //             mongoWrapper.getLogger().info(exception.toString())
    //         }
    //     })
    // } catch {
    //     case e: IllegalArgumentException => {
    //         mongoWrapper.getLogger().info(e.toString())
    //     }
    // }

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