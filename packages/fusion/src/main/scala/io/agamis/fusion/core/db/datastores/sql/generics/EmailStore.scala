package io.agamis.fusion.core.db.datastores.sql.generics

import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper

import io.agamis.fusion.core.db.datastores.typed.SqlStore

import io.agamis.fusion.core.db.common.Utils

import io.agamis.fusion.core.db.datastores.sql.generics.exceptions.emails.{
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

import io.agamis.fusion.core.db.models.sql.generics.Email
import java.util.UUID
import scala.jdk.CollectionConverters._

class EmailStore(implicit wrapper: IgniteClientNodeWrapper) extends SqlStore[UUID, Email] {

  override val schema: String = "FUSION"
  override val cache: String = s"SQL_${schema}_EMAIL"
  override protected val igniteCache: IgniteCache[UUID, Email] = if (wrapper.cacheExists(cache)) {
    wrapper.getCache[UUID, Email](cache)
  } else {
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

  def makeEmail: Email = {
    new Email
  }

  def getEmail(key: UUID)(implicit ec: ExecutionContext): Future[Email] = {
    Utils.igniteToScalaFuture(igniteCache.getAsync(key)).transformWith({
      case Success(email) =>
        email match {
          case t: Email => Future.successful(t)
          case null => Future.failed(EmailNotFoundException(s"Email $key couldn't be found"))
        }
      case Failure(cause) => Future.failed(cause)
    })
  }

  def getEmails(keys: List[UUID])(implicit ec: ExecutionContext): Future[List[Email]] = {
    Utils.igniteToScalaFuture(igniteCache.getAllAsync(keys.toSet[UUID].asJava)).transformWith({
      case Success(emails) =>
        var failedKey: UUID = null
        if (emails.isEmpty) return Future.failed(EmailNotFoundException())
        if (keys.exists(key => {
          failedKey = key;
          emails.containsKey(key)
        }))
          return Future.failed(EmailNotFoundException(s"Email $failedKey couldn't be found"))
        Future.successful(emails.values.asScala.toList)
      case Failure(cause) => Future.failed(cause)
    })
  }

  def updateEmail(email: Email)(implicit ec: ExecutionContext): Future[Unit] = {
    Utils.igniteToScalaFuture(igniteCache.putAsync(
      email.id, email
    )).transformWith({
      case Success(_) => Future.unit
      case Failure(cause) => Future.failed(EmailNotPersistedException(cause))
    })
  }

  def deleteEmail(key: UUID)(implicit ec: ExecutionContext): Future[Unit] = {
    Utils.igniteToScalaFuture(igniteCache.removeAsync(key)).transformWith({
      case Success(done) =>
        if (done) Future.unit
        else Future.failed(EmailNotPersistedException())
      case Failure(cause) => Future.failed(EmailNotPersistedException(cause))
    })
  }

  def bulkDeleteEmails(keys: List[UUID])(implicit ec: ExecutionContext): Future[Unit] = {
    Utils.igniteToScalaFuture(igniteCache.removeAllAsync(keys.toSet.asJava)).transformWith({
      case Success(_) => Future.unit
      case Failure(cause) => Future.failed(EmailNotPersistedException(cause))
    })
  }
}