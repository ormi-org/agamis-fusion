package io.ogdt.fusion.core.db.mongo

import io.ogdt.fusion.env.EnvContainer
import io.ogdt.fusion.core.db.mongo.exceptions.MissingMongoConfException

import com.typesafe.config.ConfigException

import scala.concurrent.{Future, ExecutionContext}

import akka.actor.typed.ActorSystem
import akka.actor.typed.Extension
import akka.actor.typed.ExtensionId

import reactivemongo.api.{ Cursor, DB, MongoConnection, AsyncDriver }


class ReactiveMongoWrapper(system: ActorSystem[_]) extends Extension {

    var mongoUri: String = ""

    import ExecutionContext.Implicits.global

    try {
        mongoUri = EnvContainer.getString("MONGO_URI")
    } catch {
        case e: ConfigException => {
            throw new MissingMongoConfException("MONGO_URI Config is missing", e)
        }
        case _: Throwable => throw new UnknownError("An unkown error occured while setting mongo uri")
    }

    private val driver: AsyncDriver = AsyncDriver()

    private val parsedUri = MongoConnection.fromString(mongoUri)

    private val _mongo: Future[MongoConnection] = parsedUri.flatMap(driver.connect(_))

    def getDb(db: String): Future[DB] = {
        _mongo.flatMap(_.database(db))
    }

    def getCollection(db: String, collection: String) = {
        getDb(db).map(_.collection(collection))
    }
}

object ReactiveMongoWrapper extends ExtensionId[ReactiveMongoWrapper] {
    // will only be called once
    def createExtension(system: ActorSystem[_]): ReactiveMongoWrapper = new ReactiveMongoWrapper(system)

    // Java API
    def get(system: ActorSystem[_]): ReactiveMongoWrapper = apply(system)
}
