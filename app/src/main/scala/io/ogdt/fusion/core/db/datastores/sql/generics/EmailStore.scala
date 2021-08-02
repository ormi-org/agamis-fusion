package io.ogdt.fusion.core.db.datastores.sql.generics

import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper

import io.ogdt.fusion.core.db.datastores.typed.SqlStore

import io.ogdt.fusion.core.db.common.Utils

import io.ogdt.fusion.core.db.datastores.sql.generics.exceptions.emails.{
    EmailNotFoundException,
    EmailNotPersistedException
}

import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.cache.CacheAtomicityMode

import scala.util.Success
import scala.util.Failure
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

import io.ogdt.fusion.core.db.models.sql.generics.Email
import java.util.UUID
import java.sql.Timestamp

class EmailStore(implicit wrapper: IgniteClientNodeWrapper) extends SqlStore[UUID, Email] {

    override val schema: String = "FUSION"
    override val cache: String = s"SQL_${schema}_EMAIL"
    override protected var igniteCache: IgniteCache[UUID, Email] = wrapper.cacheExists(cache) match {
        case true => wrapper.getCache[UUID, Email](cache)
        case false => {
            wrapper.createCache[UUID, Email](
                wrapper.makeCacheConfig[UUID, Email]
                .setCacheMode(CacheMode.REPLICATED)
                .setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL)
                .setDataRegionName("Fusion")
                .setName(cache)
                .setSqlSchema(schema)
                .setIndexedTypes(classOf[UUID], classOf[Email])
            )
        }
    }

    def makeEmail: Email = {
        new Email
    }

    def getEmail(key: UUID)(implicit ec: ExecutionContext): Future[Email] = {
        Utils.igniteToScalaFuture(igniteCache.getAsync(key)).transformWith({
            case Success(text) => {
                text match {
                    case t: Email => Future.successful(t)
                    case null => Future.failed(new EmailNotFoundException(s"Email ${key} couldn't be found"))
                }
            }
            case Failure(cause) => Future.failed(cause)
        })
    }

    def updateEmail(email: Email)(implicit ec: ExecutionContext): Future[Unit] = {
        Utils.igniteToScalaFuture(igniteCache.putAsync(
            email.id, email
        )).transformWith({
            case Success(value) => Future.unit
            case Failure(cause) => Future.failed(EmailNotPersistedException(cause))
        })
    }

    def deleteEmail(key: UUID)(implicit ec: ExecutionContext): Future[Unit] = {
        Utils.igniteToScalaFuture(igniteCache.removeAsync(key)).transformWith({
            case Success(done) => {
                if (done) Future.unit
                else Future.failed(EmailNotPersistedException())
            }
            case Failure(cause) => Future.failed(EmailNotPersistedException(cause))
        })
    }
}