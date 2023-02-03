package io.agamis.fusion.core.db.models.documents.file.metadata

import io.agamis.fusion.core.db.models.documents.file.metadata.meta.FusionXmlMeta
import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

import java.time.Instant
import scala.util.Try

final case class Metadata(
    size: Option[Int],
    creationDate: Instant,
    lastVersionDate: Option[Instant],
    lastModificationDate: Instant,
    chainsCount: Option[Int],
    versionsCount: Option[Int],
    fusionXML: Option[FusionXmlMeta],
    hidden: Boolean,
    readonly: Boolean
)

object Metadata {
    implicit object MetadataReader extends BSONDocumentReader[Metadata] {

        override def readDocument(doc: BSONDocument): Try[Metadata] = for {
            creationDate <- doc.getAsTry[Instant]("creationDate")
            size = doc.getAsOpt[Int]("size")
            lastVersionDate = doc.getAsOpt[Instant]("lastVersionDate")
            lastModificationDate <- doc.getAsTry[Instant]("lastModificationDate")
            chainsCount = doc.getAsOpt[Int]("chainsCount")
            versionsCount = doc.getAsOpt[Int]("versionsCount")
            fusionXML = doc.getAsOpt[FusionXmlMeta]("fusionXML")
            hidden <- doc.getAsTry[Boolean]("hidden")
            readonly <- doc.getAsTry[Boolean]("readonly")
        } yield Metadata(
            size,
            creationDate,
            lastVersionDate,
            lastModificationDate,
            chainsCount,
            versionsCount,
            fusionXML,
            hidden,
            readonly)
    }

    implicit object MetaDataWriter extends BSONDocumentWriter[Metadata] {

        override def writeTry(metadata: Metadata): Try[BSONDocument] =
            scala.util.Success(BSONDocument(
                "size" -> metadata.size,
                "creationDate" -> metadata.creationDate,
                "lastVersionDate" -> metadata.lastVersionDate,
                "lastModificationDate" -> metadata.lastModificationDate,
                "chainsCount" -> metadata.chainsCount,
                "versionsCount" -> metadata.versionsCount,
                "fusionXML" -> metadata.fusionXML,
                "hidden" -> metadata.hidden,
                "readonly" -> metadata.readonly
            ))
    }
}