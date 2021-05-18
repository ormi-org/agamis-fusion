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

    def makeTransaction(implicit ec: ExecutionContext): Try[Transaction] = {
        Try(wrapper.ignite.transactions().txStart(TransactionConcurrency.PESSIMISTIC, TransactionIsolation.READ_COMMITTED))
    }

    def commitTransaction(tx: Try[Transaction])(implicit ec: ExecutionContext): Future[Void] = {
        tx match {
            case Success(tx) => Utils.igniteToScalaFuture(tx.commitAsync())
            case Failure(cause) => Future.failed(cause)
        }
    }

    def rollbackTransaction(tx: Try[Transaction])(implicit ec: ExecutionContext): Future[Void] = {
        tx match {
            case Success(tx) => Utils.igniteToScalaFuture(tx.rollbackAsync())
            case Failure(cause) => Future.failed(cause)
        }
    }
}