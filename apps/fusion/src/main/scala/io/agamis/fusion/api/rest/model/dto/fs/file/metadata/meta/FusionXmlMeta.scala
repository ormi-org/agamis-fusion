package io.agamis.fusion.api.rest.model.dto.fs.file.metadata.meta

import io.agamis.fusion.core.db.models.documents.file.metadata.meta.FusionXmlMeta
import spray.json.DefaultJsonProtocol
import spray.json.RootJsonFormat

import java.util.UUID

final case class FusionXmlMetaDto(
    xmlSchemaFileId: String,
    originAppId: String
)

object FusionXmlMetaDto {

    implicit def apply(dto: FusionXmlMetaDto): FusionXmlMeta = {
        FusionXmlMeta(
          UUID.fromString(dto.xmlSchemaFileId),
          dto.originAppId
        )
    }

    implicit def apply(doc: FusionXmlMeta): FusionXmlMetaDto = {
        FusionXmlMetaDto(
          doc.xmlSchemaFileId.toString,
          doc.originAppId
        )
    }

}

object FusionXmlMetaDtoJsonProtocol extends DefaultJsonProtocol {

    implicit val fusionXmlFormat: RootJsonFormat[FusionXmlMetaDto] =
        jsonFormat2(FusionXmlMetaDto.apply)
}
