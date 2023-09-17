package io.agamis.fusion.api.rest.model.dto.fs.file.metadata.meta

import io.agamis.fusion.core.db.models.documents.file.metadata.meta.FusionXmlMeta
import io.agamis.fusion.api.rest.model.dto.common.JsonFormatters._
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import java.util.UUID
import scala.language.implicitConversions

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

    implicit val fusionXmlFormat: RootJsonFormat[FusionXmlMetaDto] = jsonFormat2(FusionXmlMetaDto.apply)
}