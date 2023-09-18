package io.agamis.fusion.core.data

import scala.concurrent.Future
import scala.util.{Success,Failure}
import scala.concurrent.ExecutionContext

import java.security.KeyPairGenerator
import java.security.KeyPair

import org.slf4j.{LoggerFactory,Logger}

import reactivemongo.api.bson.BSONObjectID

import io.agamis.fusion.core.db.wrappers.mongo.ReactiveMongoWrapper
import io.agamis.fusion.core.data.security.models.AsymmetricEncryption
import io.agamis.fusion.core.data.security.datastores.AsymmetricEncryptionStore

// Date & Timer 
import java.util.Date

object SystemSettings {

    var logger: Logger = LoggerFactory.getLogger(getClass())

    def insertKeyPair(privateKey: String, publicKey: String)(implicit wrapper: ReactiveMongoWrapper, ec: ExecutionContext): Future[Unit] = {
        val kpg: KeyPairGenerator = KeyPairGenerator.getInstance("RSA")
        kpg.initialize(2048)
        kpg.generateKeyPair()
        new AsymmetricEncryptionStore()
            .insert(new AsymmetricEncryption(BSONObjectID.generate(),privateKey,publicKey, new Date().toString())).transformWith({
                case Success(_) => Future.unit
                case Failure(cause) => Future.failed(new Exception("Cannot generate key", cause)) // TODO : changer pour une custom
            })
    }

    def updateKeyPair(keyPair: AsymmetricEncryption)(implicit wrapper: ReactiveMongoWrapper, ec: ExecutionContext): Future[AsymmetricEncryption] = {
        new AsymmetricEncryptionStore()
        .update(keyPair).transformWith({
            case Success(keyPair) => {
                keyPair match {
                    case Some(keyPair: AsymmetricEncryption) => Future.successful(keyPair)
                    case None => Future.failed(new Exception("Key pair not found")) // TODO : changer pour une custom
                }
            }
            case Failure(cause) =>  Future.failed(new Exception("bla bla bla",cause)) // TODO : changer pour une custom
        })
    }

    def deleteKeyPair(keyPair: AsymmetricEncryption)(implicit wrapper: ReactiveMongoWrapper, ec: ExecutionContext): Future[AsymmetricEncryption] = {
        new AsymmetricEncryptionStore()
        .delete(keyPair).transformWith({
            case Success(deletedKeyPair) => {
                deletedKeyPair match {
                    case Some(matchingAE: AsymmetricEncryption) => Future.successful(matchingAE)
                    case None => Future.failed(new Exception("Key pair not found")) // TODO : changer pour une custom
                }
            }
            case Failure(cause) => Future.failed(new Exception("bla bla bla",cause)) // TODO : changer pour une custom
        })
    }

}