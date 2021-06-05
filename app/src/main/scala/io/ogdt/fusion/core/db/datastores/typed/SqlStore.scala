package io.ogdt.fusion.core.db.datastores.typed

import scala.jdk.CollectionConverters._

import org.apache.ignite.IgniteCache
import org.apache.ignite.transactions.Transaction

import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper

import org.apache.ignite.transactions.TransactionConcurrency
import org.apache.ignite.transactions.TransactionIsolation

import scala.util.Try
import scala.util.Success
import scala.util.Failure
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

import io.ogdt.fusion.core.db.common.Utils

abstract class SqlStore[K, M](implicit wrapper: IgniteClientNodeWrapper) {

    val schema: String
    val cache: String
    protected var igniteCache: IgniteCache[K, M]

    /** A method for initiating a new Ignite K/V transaction
      *
      * @param ec implicit [[scala.concurrent.ExecutionContext ExecutionContext]]
      * @return transaction instance
      */
    def makeTransaction(implicit ec: ExecutionContext): Try[Transaction] = {
        Try(wrapper.ignite.transactions().txStart(TransactionConcurrency.PESSIMISTIC, TransactionIsolation.READ_COMMITTED))
    }

    /** A method for commiting an existing transaction resulting in writing modifications to cache
      *
      * @param tx a [[org.apache.ignite.transactions.Transaction Transaction]] instance to commit
      * @param ec implicit [[scala.concurrent.ExecutionContext ExecutionContext]]
      * @return a future result
      */
    def commitTransaction(tx: Try[Transaction])(implicit ec: ExecutionContext): Future[Void] = {
        tx match {
            case Success(tx) => Utils.igniteToScalaFuture(tx.commitAsync())
            case Failure(cause) => Future.failed(cause)
        }
    }

    /** A method for rolling back a transaction thus canceling all uncommited operations
      *
      * @param tx a [[org.apache.ignite.transactions.Transaction Transaction]] instance to rollback
      * @param ec implicit [[scala.concurrent.ExecutionContext ExecutionContext]]
      * @return a future result
      */
    def rollbackTransaction(tx: Try[Transaction])(implicit ec: ExecutionContext): Future[Void] = {
        tx match {
            case Success(tx) => Utils.igniteToScalaFuture(tx.rollbackAsync())
            case Failure(cause) => Future.failed(cause)
        }
    }
}