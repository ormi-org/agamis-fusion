package io.ogdt.fusion.core.db.models.sql.typed

import java.util.UUID
import scala.concurrent.Future
import scala.util.Success
import scala.concurrent.ExecutionContext
import scala.util.Failure
import io.ogdt.fusion.core.db.datastores.typed.SqlStore
import io.ogdt.fusion.core.db.models.sql.typed.annotations.{PrePersist, PostPersist, PreRemove, PostRemove}

trait Model {

    genId()

    protected var _id: UUID
    private def genId() = {
        this._id = this._id match {
            case null => UUID.randomUUID()
            case x => x
        }
    }

    protected def persist(method: () => Future[Boolean])(implicit ec: ExecutionContext): Future[Boolean] = {
        // prePersist.transformWith({
        //     case Success(preResult) =>
        //         if (preResult) return Future.successful(true)
        //         return Future.failed(new Error("Persist pre-method failed")) // TODO : replace with custom Exception
        //     case Failure(cause) => Future.failed(cause)
        // })
        Future.sequence(List(
            PrePersist.executeAnnotatedMethods(this),
            method().transformWith({
                case Success(result) =>
                    if (result) return Future.successful(true)
                    return Future.failed(new Error("Persist core method failed")) // TODO : replace with custom Exception
                case Failure(cause) => Future.failed(cause)
            }),
            PostPersist.executeAnnotatedMethods(this)
        )).transformWith({
            case Success(results) => Future.successful(true)
            case Failure(cause) => Future.failed(cause)
        })
        // PostPersist.executeAnnotatedMethods(this)
        // postPersist.transformWith({
        //     case Success(postResult) => 
        //         if (postResult) return Future.successful(true)
        //         return Future.failed(new Error("Persist post-method failed")) // TODO : replace with custom Exception
        //     case Failure(cause) => Future.failed(cause)
        // })
    }

    protected def remove(method: () => Future[Boolean])(implicit ec: ExecutionContext): Future[Boolean] = {
        // preRemove.transformWith({
        //     case Success(preResult) =>
        //         if (preResult) return Future.successful(true)
        //         return Future.failed(new Error("Remove core method failed")) // TODO : replace with custom Exception
        //     case Failure(cause) => Future.failed(cause)
        // })
        Future.sequence(List(
            PreRemove.executeAnnotatedMethods(this),
            method().transformWith({
                case Success(result) =>
                    if (result) return Future.successful(true)
                    return Future.failed(new Error("Remove core method failed")) // TODO : replace with custom Exception
                case Failure(cause) => Future.failed(cause)
            }),
            PostRemove.executeAnnotatedMethods(this)
        )).transformWith({
            case Success(results) => Future.successful(true)
            case Failure(cause) => Future.failed(cause)
        })
        // postRemove.transformWith({
        //     case Success(postResult) => 
        //         if (postResult) return Future.successful(true)
        //         return Future.failed(new Error("Remove post-method failed")) // TODO : replace with custom Exception
        //     case Failure(cause) => Future.failed(cause)
        // })
    }

    // def prePersist: Future[Boolean] = {
    //     Future.successful(true)
    // }

    // def postPersist: Future[Boolean] = {
    //     Future.successful(true)
    // }

    // def preRemove: Future[Boolean] = {
    //     Future.successful(true)
    // }

    // def postRemove: Future[Boolean] = {
    //     Future.successful(true)
    // }
}
