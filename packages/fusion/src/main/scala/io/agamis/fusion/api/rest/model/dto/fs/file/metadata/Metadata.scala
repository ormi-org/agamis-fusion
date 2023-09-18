package io.agamis.fusion.api.rest.model.dto.fs.file.metadata

import io.agamis.fusion.api.rest.model.dto.fs.file.metadata.meta.FusionXmlMetaDto
import io.agamis.fusion.api.rest.model.dto.fs.file.metadata.meta.FusionXmlMetaDtoJsonProtocol._
import io.agamis.fusion.core.db.models.documents.file.metadata.Metadata
import io.agamis.fusion.core.db.models.documents.file.metadata.meta.FusionXmlMeta
import spray.json._

import java.time.Instant

final case class MetadataDto(
    size: Option[Int],
    creationDate: String,
    lastVersionDate: Option[String],
    lastModificationDate: String,
    chainsCount: Option[Int],
    versionsCount: Option[Int],
    fusionXml: Option[FusionXmlMetaDto],
    hidden: Boolean,
    readonly: Boolean
)

object MetadataDto {

    implicit def apply(dto: MetadataDto): Metadata = {
        Metadata(
          dto.size,
          Instant.parse(dto.creationDate),
          dto.lastVersionDate.orNull match {
              case lastVersionDate: String =>
                  Some(Instant.parse(lastVersionDate))
              case null => None
          },
          Instant.parse(dto.lastModificationDate),
          dto.chainsCount,
          dto.versionsCount,
          dto.fusionXml.orNull match {
              case fXmlMetaDto: FusionXmlMetaDto =>
                  Some(FusionXmlMetaDto(fXmlMetaDto))
              case null => None
          },
          dto.hidden,
          dto.readonly
        )
    }

    implicit def apply(doc: Metadata): MetadataDto = {
        MetadataDto(
          doc.size,
          doc.creationDate.toString,
          doc.lastVersionDate.orNull match {
              case lastVersionDate: Instant => Some(lastVersionDate.toString)
              case null                     => None
          },
          doc.lastModificationDate.toString,
          doc.chainsCount,
          doc.versionsCount,
          doc.fusionXML.orNull match {
              case fXmlMeta: FusionXmlMeta => Some(FusionXmlMetaDto(fXmlMeta))
              case null                    => None
          },
          doc.hidden,
          doc.readonly
        )
    }
}

object MetadataDtoJsonProtocol extends DefaultJsonProtocol {

    implicit val metadataFormat: RootJsonFormat[MetadataDto] = jsonFormat9(
      MetadataDto.apply
    )
}
