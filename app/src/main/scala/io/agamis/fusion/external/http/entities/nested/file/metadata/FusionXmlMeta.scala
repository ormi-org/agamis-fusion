package io.agamis.fusion.external.http.entities.nested.file.metadata

import java.util.UUID
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import io.agamis.fusion.external.http.entities.common.JsonFormatters._
import io.agamis.fusion.core.db.models.documents.nested.file.metadata.{FusionXmlMeta => FusionXmlMetaDocument}

import scala.language.implicitConversions

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

    implicit val fusionXmlFormat: RootJsonFormat[FusionXmlMeta] = jsonFormat2(FusionXmlMeta.apply)
}