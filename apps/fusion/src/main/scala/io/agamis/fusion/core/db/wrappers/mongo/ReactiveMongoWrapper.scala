package io.agamis.fusion.core.db.wrappers.mongo

import io.agamis.fusion.env.EnvContainer
import io.agamis.fusion.core.db.wrappers.mongo.exceptions.MissingMongoConfException

import com.typesafe.config.ConfigException

import scala.concurrent.{Future, ExecutionContext}

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.Extension
import org.apache.pekko.actor.typed.ExtensionId

import reactivemongo.api.{DB, MongoConnection, AsyncDriver}
import reactivemongo.api.bson.collection.BSONCollection

class ReactiveMongoWrapper(system: ActorSystem[_]) extends Extension {

    implicit val ec: ExecutionContext = system.executionContext

    var mongoUri: String = null

    try {
        mongoUri = EnvContainer.getString("fusion.core.db.mongo.uri")
    } catch {
        case e: ConfigException => {
            throw new MissingMongoConfException(
              "fusion.core.db.mongo.uri Config is missing",
              e
            )
        }
        case _: Throwable =>
            throw new UnknownError(
              "An unkown error occurred while setting mongo uri"
            )
    }

    private val driver: AsyncDriver = AsyncDriver()

    private val parsedUri = MongoConnection.fromString(mongoUri)

    private val _mongo: Future[MongoConnection] =
        parsedUri.flatMap(driver.connect(_))

    def getDb(db: String): Future[DB] = {
        _mongo.flatMap(_.database(db))
    }

    def getCollection(
        db: String,
        collection: String
    ): Future[BSONCollection] = {
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
    def createExtension(system: ActorSystem[_]): ReactiveMongoWrapper =
        new ReactiveMongoWrapper(system)

    // Java API
    def get(system: ActorSystem[_]): ReactiveMongoWrapper = apply(system)
}
