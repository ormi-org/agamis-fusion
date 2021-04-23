package io.ogdt.fusion.external.http.entities.nested.file.acl.access

import spray.json.{DefaultJsonProtocol, RootJsonFormat, JsObject, JsValue, DeserializationException}
import spray.json.JsBoolean
import spray.json.JsonFormat
import spray.json.JsArray

final case class Rights(
    read: Boolean,
    readAndExecute: Boolean,
    write: Boolean,
    versioning: Option[Boolean],
    advancedVersioning: Option[Boolean],
    aclManagement: Boolean,
    advancedAclManagement: Boolean,
    totalControl: Boolean
)

object RightsJsonProtocol extends DefaultJsonProtocol{

    implicit object RightsJsonFormat extends JsonFormat[Rights] {
        
        def read(value: JsValue) = {
            value.asJsObject.getFields("read", "readAndExecute", "write", "versionning", "advancedVersioning", "aclManagement", "advancedAclManagement", "totalControl") match {
                case Seq(JsBoolean(read), JsBoolean(readAndExecute), JsBoolean(write), JsBoolean(versionning), JsBoolean(advancedVersioning), JsBoolean(aclManagement), JsBoolean(advancedAclManagement), JsBoolean(totalControl)) =>
                    new Rights(read, readAndExecute, write, Some(versionning), Some(advancedVersioning), aclManagement, advancedAclManagement, totalControl)
                case _ => throw new DeserializationException("Rights expected")
            }
        }
       
       def write(r: Rights) = JsObject(
           "read"                  -> JsBoolean(r.read),
           "readAndExecute"        -> JsBoolean(r.readAndExecute),
           "write"                 -> JsBoolean(r.write),
           "versioning"            -> JsBoolean(r.versioning.get),
           "advancedVersioning"    -> JsBoolean(r.advancedVersioning.get),
           "aclManagement"         -> JsBoolean(r.aclManagement),
           "advancedAclManagement" -> JsBoolean(r.advancedAclManagement),
           "totalControl"          -> JsBoolean(r.totalControl)
       )
    }
}
