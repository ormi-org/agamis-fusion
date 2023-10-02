package io.agamis.fusion.core.db.datastore.typed

import io.agamis.fusion.core.db.common.Utils
import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import org.apache.ignite.IgniteCache
import org.apache.ignite.transactions.Transaction
import org.apache.ignite.transactions.TransactionConcurrency
import org.apache.ignite.transactions.TransactionIsolation

import scala.concurrent.Future
import scala.util.Try

abstract class IgniteStore[K, M](implicit wrapper: IgniteClientNodeWrapper) {

    val schema: String
    val cache: String
    protected var igniteCache: IgniteCache[K, M]

    /** A method for initiating a new Ignite K/V transaction
      *
      * @param ec
      *   implicit [[scala.concurrent.ExecutionContext ExecutionContext]]
      * @return
      *   transaction instance
      */
    def makeTransaction: Try[Transaction] = {
        Try(
          wrapper.ignite
              .transactions()
              .txStart(
                TransactionConcurrency.PESSIMISTIC,
                TransactionIsolation.READ_COMMITTED
              )
        )
    }

    /** A method for commiting an existing transaction resulting in writing
      * modifications to cache
      *
      * @param tx
      *   a [[org.apache.ignite.transactions.Transaction Transaction]] instance
      *   to commit
      * @param ec
      *   implicit [[scala.concurrent.ExecutionContext ExecutionContext]]
      * @return
      *   a future result
      */
    def commitTransaction(tx: Transaction): Future[Void] = {
        Utils.igniteToScalaFuture(tx.commitAsync())
    }

    /** A method for rolling back a transaction thus canceling all uncommited
      * operations
      *
      * @param tx
      *   a [[org.apache.ignite.transactions.Transaction Transaction]] instance
      *   to rollback
      * @param ec
      *   implicit [[scala.concurrent.ExecutionContext ExecutionContext]]
      * @return
      *   a future result
      */
    def rollbackTransaction(tx: Transaction): Future[Void] = {
        Utils.igniteToScalaFuture(tx.rollbackAsync())
    }
}
