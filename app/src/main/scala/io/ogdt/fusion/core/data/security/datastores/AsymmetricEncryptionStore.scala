package io.ogdt.fusion.core.data.security.datastores

import scala.concurrent.Future

import io.ogdt.fusion.core.db.wrappers.mongo.ReactiveMongoWrapper
import io.ogdt.fusion.core.db.datastores.typed.DocumentStore
import io.ogdt.fusion.core.data.security.models.AsymmetricEncryption

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import reactivemongo.api.commands.WriteResult
import java.util.Timer
import io.ogdt.fusion.core.db.datastores.documents.aggregations.typed.Pipeline

class AsymmetricEncryptionGeneratorStore(implicit wrapper: ReactiveMongoWrapper) extends DocumentStore[AsymmetricEncryption] {
    override val database: String = "fusiondb"
    override val collection: String = "systemsettings"

    // DEBUG
    val logger: Logger = LoggerFactory.getLogger(getClass())
    
    override def insert(keys: AsymmetricEncryption): Future[WriteResult] = ???

    override def insertMany(keys: List[AsymmetricEncryption]): Future[Int] = ???

    override def updateMany(keys: List[AsymmetricEncryption]): Future[Int] = ???

    override def update(keys: AsymmetricEncryption): Future[Option[AsymmetricEncryption]] = ???

    override def deleteMany(keys: List[AsymmetricEncryption]): Future[Int] = ???

    override def delete(keys: AsymmetricEncryption): Future[Option[AsymmetricEncryption]] = ???

    override def aggregate(pipeline: Pipeline): Future[List[AsymmetricEncryption]] = ???

    def geneneration(keys: AsymmetricEncryption) = ???

}
