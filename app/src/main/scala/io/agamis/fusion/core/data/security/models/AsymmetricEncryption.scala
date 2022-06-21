package io.agamis.fusion.core.data.security.models

import reactivemongo.api.bson.BSONObjectID
import reactivemongo.api.bson.BSONValue
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.BSONDocumentReader
import reactivemongo.api.bson.BSONDocumentWriter

import scala.util.Try

final case class AsymmetricEncryption(
    id: BSONObjectID,
    privateKey: String, 
    publicKey: String,
    date: String
)

object AsymmetricEncryption {
    
    implicit object AsymmetricEncryptionReader extends BSONDocumentReader[AsymmetricEncryption] {

        override def readDocument(doc: BSONDocument): Try[AsymmetricEncryption] = for {
            id <- doc.getAsTry[BSONObjectID]("_id")
            privateKey <- doc.getAsTry[String]("privateKey")
            publicKey <- doc.getAsTry[String]("publicKey")
            date <- doc.getAsTry[String]("date")
        } yield AsymmetricEncryption(id, privateKey, publicKey, date)

    }

    implicit object AsymmetricEncryptionWriter extends BSONDocumentWriter[AsymmetricEncryption] {
        
        override def writeTry(ae: AsymmetricEncryption): Try[BSONDocument] = 
            scala.util.Success(BSONDocument(
                "_id" -> ae.id, 
                "privateKey" -> ae.privateKey, 
                "publicKey" -> ae.publicKey, 
                "date" -> ae.date
            ))

    }

}