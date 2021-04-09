package io.ogdt.fusion.core.db.wrappers.mongo

import io.ogdt.fusion.env.EnvContainer
import io.ogdt.fusion.core.db.wrappers.mongo.exceptions.MissingMongoConfException

import com.typesafe.config.ConfigException

import scala.util.{Failure, Success}
import scala.concurrent.{Future, ExecutionContext}

import akka.event.Logging
import akka.actor.typed.ActorSystem
import akka.actor.typed.Extension
import akka.actor.typed.ExtensionId

import reactivemongo.api.{ Cursor, DB, MongoConnection, AsyncDriver }
import reactivemongo.api.bson.collection.BSONCollection

import org.slf4j.Logger

class ReactiveMongoWrapper(system: ActorSystem[_]) extends Extension {

    var mongoUri: String = null

    import ExecutionContext.Implicits.global

    try {
        mongoUri = EnvContainer.getString("fusion.core.db.mongo.uri")
    } catch {
        case e: ConfigException => {
            throw new MissingMongoConfException("fusion.core.db.mongo.uri Config is missing", e)
        }
        case _: Throwable => throw new UnknownError("An unkown error occured while setting mongo uri")
    }

    private val driver: AsyncDriver = AsyncDriver()

    private val parsedUri = MongoConnection.fromString(mongoUri)

    private val _mongo: Future[MongoConnection] = parsedUri.flatMap(driver.connect(_))

    def getLogger(): Logger = system.log

    def getDb(db: String): Future[DB] = {
        _mongo.flatMap(_.database(db))
    }

    def getCollection(db: String, collection: String): Future[BSONCollection] = {
        getDb(db).map(_.collection(collection))
    }

    // def collectionExists(db: String, collection: String): Unit = {
    //     getCollection(db, collection).onComplete({
    //         case Success(col) => {
    //             system.log.info("Col fetched")
    //             system.log.info(col.toString())
    //         }
    //         case Failure(exception) => {
    //             system.log.info("Col fetching exception")
    //             system.log.info(exception.toString())
    //         }
    //     })
    // }
}

object ReactiveMongoWrapper extends ExtensionId[ReactiveMongoWrapper] {
    // will only be called once
    def createExtension(system: ActorSystem[_]): ReactiveMongoWrapper = new ReactiveMongoWrapper(system)

    // Java API
    def get(system: ActorSystem[_]): ReactiveMongoWrapper = apply(system)
}
