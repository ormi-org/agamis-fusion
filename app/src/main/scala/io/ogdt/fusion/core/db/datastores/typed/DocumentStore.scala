package io.ogdt.fusion.core.db.datastores.typed

import io.ogdt.fusion.core.db.wrappers.mongo.ReactiveMongoWrapper
import io.ogdt.fusion.core.db.datastores.documents.aggregations.typed.Pipeline

import scala.util.{Success, Failure, Try}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import reactivemongo.api.DB
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.commands.WriteResult

abstract class DocumentStore[M](implicit wrapper: ReactiveMongoWrapper) {

    val database: String
    val collection: String

    def insert(docObject: M): Future[WriteResult]

    def insertMany(docObject: List[M]): Future[_]

    def aggregate(pipeline: Pipeline): Future[List[_]]

    def startTransaction(): Future[DB] = {
        wrapper.getDb(database).transformWith {
            case Success(db) => db.startSession().transformWith[DB] {
                case Success(dbWithSession) => dbWithSession.startTransaction(None).transform {
                    case Success(dbWithTx) => Try(dbWithTx)
                    case Failure(cause) => Failure(new Exception(cause))
                }
                case Failure(cause) => throw new Exception(cause)
            }
            case Failure(cause) => throw new Exception(cause)
        }
    }
}
