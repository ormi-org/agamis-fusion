package io.ogdt.fusion.external.http.entities.nested.file.metadata

import java.util.UUID

import spray.json.DefaultJsonProtocol

import io.ogdt.fusion.external.http.entities.common.JsonFormatters._

import io.ogdt.fusion.core.db.models.documents.nested.file.metadata.{FusionXmlMeta => FusionXmlMetaDocument}

final case class FusionXmlMeta(
    xmlSchemaFileId: UUID,
    originAppId: String
)

object FusionXmlMeta { 

    implicit def fusionXmlMetaToDocument(fxm: FusionXmlMeta): FusionXmlMetaDocument = {
        FusionXmlMetaDocument(
            fxm.xmlSchemaFileId, 
            fxm.originAppId
        )
    }

    implicit def documentToFusionXmlMeta(doc: FusionXmlMetaDocument): FusionXmlMeta = {
        FusionXmlMeta(
            doc.xmlSchemaFileId, 
            doc.originAppId
        )
    }

}

object FusionXmlMetaJsonProtocol extends DefaultJsonProtocol {

    implicit val fusionXmlFormat = jsonFormat2(FusionXmlMeta.apply)
}