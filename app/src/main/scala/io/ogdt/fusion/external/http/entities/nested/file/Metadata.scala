package io.ogdt.fusion.external.http.entities.nested.file

import java.time.Instant
import java.time.format.DateTimeFormatter

import spray.json._
import spray.json.DefaultJsonProtocol._

import io.ogdt.fusion.external.http.entities.nested.file.metadata.FusionXmlMeta
import io.ogdt.fusion.external.http.entities.nested.file.metadata.FusionXmlMetaJsonProtocol._

import io.ogdt.fusion.external.http.entities.common.JsonFormatters.InstantFormat

import io.ogdt.fusion.core.db.models.documents.nested.file.{Metadata => MetadataDocument}


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

    implicit def metadataToMetadataDocument(m: Metadata): MetadataDocument = {
        MetadataDocument(
            m.size,
            m.creationDate,
            m.lastVersionDate,
            m.lastModificationDate,
            m.chainsCount,
            m.versionsCount,
            Some(m.fusionXML.getOrElse(null)),
            m.hidden,
            m.readonly
        )
    }

    implicit def documentToMetadata(doc: MetadataDocument): Metadata = {
        Metadata(
            doc.size,
            doc.creationDate,
            doc.lastVersionDate,
            doc.lastModificationDate,
            doc.chainsCount,
            doc.versionsCount,
            Some(doc.fusionXML.getOrElse(null)),
            doc.hidden,
            doc.readonly
        )
    }

}


object MetadataJsonProtocol extends DefaultJsonProtocol {

    implicit val metadataFormat = jsonFormat9(Metadata.apply)
}