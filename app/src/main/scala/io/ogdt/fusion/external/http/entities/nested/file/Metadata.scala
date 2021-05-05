package io.ogdt.fusion.external.http.entities.nested.file

import java.time.Instant
import java.time.format.DateTimeFormatter

import spray.json._
import spray.json.DefaultJsonProtocol._

import io.ogdt.fusion.external.http.entities.nested.file.metadata.FusionXmlMeta
import io.ogdt.fusion.external.http.entities.nested.file.metadata.FusionXmlMetaJsonProtocol._

import io.ogdt.fusion.external.http.entities.common.JsonFormatters.InstantFormat

import io.ogdt.fusion.core.db.models.documents.nested.file.{Metadata => MetadataDocument}
import io.ogdt.fusion.core.db.models.documents.nested.file.metadata.{FusionXmlMeta => FusionXmlMetaDocument}


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
            m.fusionXML.getOrElse(null) match {
                case meta: FusionXmlMeta => Some(meta.copy())
                case null => None
            },
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
            doc.fusionXML.getOrElse(null) match {
                case meta: FusionXmlMetaDocument => Some(meta.copy())
                case null => None
            },
            doc.hidden,
            doc.readonly
        )
    }

}


object MetadataJsonProtocol extends DefaultJsonProtocol {

    implicit val metadataFormat = jsonFormat9(Metadata.apply)
}