package io.ogdt.fusion.core.db.datastores.typed

import io.ogdt.fusion.core.db.wrappers.mongo.ReactiveMongoWrapper

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import reactivemongo.api.DB
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.commands.WriteResult
import scala.util.Success
import scala.util.Failure
import scala.util.Try

abstract class DocumentStore[M](wrapper: ReactiveMongoWrapper) {

    val database: String
    val collection: String

    def insert(docObject: M, db: Option[DB]): Future[WriteResult]

    def insertMany(docObject: M, db: Option[DB]): Future[_]

    def find(docObject: M, db: Option[DB]): Future[Option[M]]

    def findMany(docObject: M, db: Option[DB]): Future[Option[List[M]]]

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
        // db => {
        //         db.startSession().flatMap { dbWithSession =>
        //             dbWithSession.startTransaction(None).flatMap {
        //                 db => {
        //                     Future.successful(db)
        //                 }
        //             }
        //         }
        //     }
    }


}
