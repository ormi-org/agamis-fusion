package io.ogdt.fusion.core.db.models.documents.nested.file

import io.ogdt.fusion.core.db.models.documents.nested.file.metadata.FusionXmlMeta

import scala.util.Try

import java.time.Instant

import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

final case class Metadata(
    size: Int,
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
            size <- doc.getAsTry[Int]("size")
            creationDate <- doc.getAsTry[Instant]("creationDate")
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