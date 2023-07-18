package io.agamis.fusion.core.db.models.documents.file.metadata.meta

import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

import java.util.UUID
import scala.util.Try

final case class FusionXmlMeta(
    xmlSchemaFileId: UUID,
    originAppId: String
)

object FusionXmlMeta {
    implicit object FusionXmlMetaReader extends BSONDocumentReader[FusionXmlMeta] {

        override def readDocument(doc: BSONDocument): Try[FusionXmlMeta] = for {
            xmlSchemaFileId <- doc.getAsTry[UUID]("xmlSchemaFileId")
            originAppId <- doc.getAsTry[String]("originAppId")
        } yield FusionXmlMeta(xmlSchemaFileId, originAppId)
    }

    implicit object FusionXmlMetaWriter extends BSONDocumentWriter[FusionXmlMeta] {

        override def writeTry(fusionXmlMeta: FusionXmlMeta): Try[BSONDocument] =
            scala.util.Success(BSONDocument(
                "xmlSchemaFileId" -> fusionXmlMeta.xmlSchemaFileId,
                "originAppId" -> fusionXmlMeta.originAppId
            ))
    }
}