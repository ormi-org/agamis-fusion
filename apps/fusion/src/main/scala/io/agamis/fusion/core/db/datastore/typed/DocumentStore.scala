package io.agamis.fusion.core.db.datastore.typed

import io.agamis.fusion.core.db.datastore.document.aggregation.typed.Pipeline
import io.agamis.fusion.core.db.wrappers.mongo.ReactiveMongoWrapper
import reactivemongo.api.DB
import reactivemongo.api.commands.WriteResult

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import scala.util.Try

abstract class DocumentStore[M](implicit wrapper: ReactiveMongoWrapper) {

    val database: String
    val collection: String

    def insert(docObject: M)(implicit ec: ExecutionContext): Future[WriteResult]

    def insertMany(docObject: List[M])(implicit ec: ExecutionContext): Future[_]

    def update(docObject: M)(implicit ec: ExecutionContext): Future[Option[M]]

    def updateMany(docObject: List[M])(implicit ec: ExecutionContext): Future[_]

    def delete(docObject: M)(implicit ec: ExecutionContext): Future[_]

    def deleteMany(docObject: List[M])(implicit ec: ExecutionContext): Future[_]

    def aggregate(pipeline: Pipeline)(implicit
        ec: ExecutionContext
    ): Future[List[_]]

    def startTransaction()(implicit ec: ExecutionContext): Future[DB] = {
        wrapper.getDb(database).transformWith {
            case Success(db) =>
                db.startSession().transformWith[DB] {
                    case Success(dbWithSession) =>
                        dbWithSession.startTransaction(None).transform {
                            case Success(dbWithTx) => Try(dbWithTx)
                            case Failure(cause) => Failure(new Exception(cause))
                        }
                    case Failure(cause) => throw new Exception(cause)
                }
            case Failure(cause) => throw new Exception(cause)
        }
    }
}
