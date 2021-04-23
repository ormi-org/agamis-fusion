package io.ogdt.fusion.external.http.entities.nested.file.metadata

import java.util.UUID

import spray.json.{DefaultJsonProtocol, JsonFormat, JsObject, JsString, JsValue, DeserializationException}
import spray.json.JsString

final case class FusionXmlMeta(
    xmlSchemaFileId: UUID,
    originAppId: String
)

object FusionXmlMetaJsonProtocol extends DefaultJsonProtocol {

    implicit object FusionXmlMetaJsonFormat extends JsonFormat[FusionXmlMeta] {
       def read(value: JsValue) = {
           value.asJsObject.getFields("xmlSchemaFileId", "originAppId") match {
               case Seq(xmlSchemaFileId, JsString(originAppId)) => 
                new FusionXmlMeta(UUID.fromString(xmlSchemaFileId.toString()), originAppId)
               case _ => throw new DeserializationException("GroupAccess expected")
           } 
       }

        def write(fxm: FusionXmlMeta) = JsObject(
            "xmlSchemaFileId" -> JsString(fxm.xmlSchemaFileId.toString()),  
            "originAppId"  -> JsString(fxm.originAppId)
        )
    }

}