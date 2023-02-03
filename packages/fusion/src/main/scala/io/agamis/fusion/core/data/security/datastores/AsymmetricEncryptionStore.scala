package io.agamis.fusion.core.data.security.datastores

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.util.{Success,Failure}

import org.slf4j.{Logger, LoggerFactory}

import reactivemongo.api.commands.WriteResult
import reactivemongo.api.bson.BSONDocument

import io.agamis.fusion.core.db.wrappers.mongo.ReactiveMongoWrapper
import io.agamis.fusion.core.db.datastores.typed.DocumentStore
import io.agamis.fusion.core.data.security.models.AsymmetricEncryption
import io.agamis.fusion.core.db.datastores.documents.aggregations.typed.Pipeline

class AsymmetricEncryptionStore(implicit wrapper: ReactiveMongoWrapper) extends DocumentStore[AsymmetricEncryption] {

    override val database: String = "fusiondb"
    override val collection: String = "systemsettings"

    // DEBUG
    val logger: Logger = LoggerFactory.getLogger(getClass())
    
    override def insert(keys: AsymmetricEncryption)(implicit ec: ExecutionContext): Future[WriteResult] = {
        wrapper.getCollection(database,collection).transformWith({
            case Success(col) => {
                col.insert.one[AsymmetricEncryption](keys).transformWith({
                    case Success(result) => {
                        Future.successful(result)
                    }
                    case Failure(cause) => Future.failed(new Exception("Can't insert keys in database",cause)) // TODO : changer pour une custom
                })
            }
            case Failure(cause) => Future.failed(new Exception("Can't contact database server",cause)) // TODO : changer pour une custom  
        })
    }

    override def update(keys: AsymmetricEncryption)(implicit ec: ExecutionContext): Future[Option[AsymmetricEncryption]] = {
        wrapper.getCollection(database,collection).transformWith({
            case Success(col) => {
                col.findAndUpdate[BSONDocument, AsymmetricEncryption](BSONDocument("_id" -> keys.id), keys, upsert = false)
                .map(_.result[AsymmetricEncryption])
            }
            case Failure(cause) => Future.failed(new Exception("bla bla bla", cause)) //TODO : changer pour une custom
        })
    }

    override def delete(keys: AsymmetricEncryption)(implicit ec: ExecutionContext): Future[Option[AsymmetricEncryption]] = {
        wrapper.getCollection(database,collection).transformWith({
            case Success(col) => {
                col.findAndRemove[BSONDocument](BSONDocument("_id" -> keys.id))
                .map(_.result[AsymmetricEncryption])
            }
            case Failure(cause) => Future.failed(new Exception("bla bla bla",cause)) // TODO : changer pour une custom 
        })
    }

    override def insertMany(keys: List[AsymmetricEncryption])(implicit ec: ExecutionContext): Future[_] = { throw new UnsupportedOperationException("Not implemented") }
    override def updateMany(keys: List[AsymmetricEncryption])(implicit ec: ExecutionContext): Future[_] = { throw new UnsupportedOperationException("Not implemented") }
    override def deleteMany(keys: List[AsymmetricEncryption])(implicit ec: ExecutionContext): Future[_] = { throw new UnsupportedOperationException("Not implemented") }
    override def aggregate(pipeline: Pipeline)(implicit ec: ExecutionContext): Future[List[_]] = { throw new UnsupportedOperationException("Not implemented") }

}
