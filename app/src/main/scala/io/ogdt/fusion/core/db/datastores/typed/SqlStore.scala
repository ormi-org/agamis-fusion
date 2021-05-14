package io.ogdt.fusion.core.db.datastores.typed

import scala.jdk.CollectionConverters._

import org.apache.ignite.IgniteCache
import org.apache.ignite.transactions.Transaction

import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper

abstract class SqlStore[K, M](implicit wrapper: IgniteClientNodeWrapper) {

    val schema: String
    val cache: String
    protected var igniteCache: IgniteCache[K, M]

    // def makeTransaction(): Transaction = {
        
    // }

    // def commitTransaction(): Unit = {
        
    // }

    // def rollbackTransaction(tx: Transaction): Unit = {

    // }
}