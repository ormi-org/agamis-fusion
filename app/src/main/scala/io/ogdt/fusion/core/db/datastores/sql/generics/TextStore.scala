package io.ogdt.fusion.core.db.datastores.sql.generics

import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper

import io.ogdt.fusion.core.db.datastores.typed.SqlStore

import io.ogdt.fusion.core.db.common.Utils

import io.ogdt.fusion.core.db.datastores.sql.generics.exceptions.texts.{
    TextNotFoundException,
    TextNotPersistedException,
    TextLanguageNotSetException
}

import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.cache.CacheAtomicityMode

import scala.util.Success
import scala.util.Failure
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

import io.ogdt.fusion.core.db.models.sql.generics.Text
import java.util.UUID

class TextStore(implicit wrapper: IgniteClientNodeWrapper) extends SqlStore[String, Text] {

    override val schema: String = "FUSION"
    override val cache: String = s"SQL_${schema}_TEXT"
    override protected var igniteCache: IgniteCache[String,Text] = wrapper.cacheExists(cache) match {
        case true => wrapper.getCache[String, Text](cache)
        case false => {
            wrapper.createCache[String, Text](
                wrapper.makeCacheConfig[String, Text]
                .setCacheMode(CacheMode.REPLICATED)
                .setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL)
                .setDataRegionName("Fusion")
                .setName(cache)
                .setSqlSchema(schema)
                .setIndexedTypes(classOf[String], classOf[Text])
            )
        }
    }

    def makeText: Text = {
        new Text
    }

    def getText(key: String)(implicit ec: ExecutionContext): Future[Text] = {
        Utils.igniteToScalaFuture(igniteCache.getAsync(key)).transformWith({
            case Success(text) => {
                text match {
                    case t: Text => Future.successful(t)
                    case null => Future.failed(new TextNotFoundException(s"Text ${key} couldn't be found"))
                }
            }
            case Failure(cause) => Future.failed(cause)
        })
    }

    def updateText(text: Text)(implicit ec: ExecutionContext): Future[Unit] = {
        if (text.relatedLanguage.isEmpty) Future.failed(new TextLanguageNotSetException)
        Utils.igniteToScalaFuture(igniteCache.putAsync(
            s"${text.id}:${text.relatedLanguage.get.id}", text
        )).transformWith({
            case Success(value) => Future.unit
            case Failure(cause) => Future.failed(TextNotPersistedException(cause))
        })
    }
}