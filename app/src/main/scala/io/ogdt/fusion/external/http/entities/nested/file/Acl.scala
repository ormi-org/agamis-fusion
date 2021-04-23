package io.ogdt.fusion.external.http.entities.nested.file

import spray.json.{DefaultJsonProtocol, JsonFormat, JsObject, JsValue, JsArray, DeserializationException}

import io.ogdt.fusion.external.http.entities.nested.file.acl.{GroupAccess, UserAccess}
final case class Acl(
    userAccess: List[UserAccess],
    groupAccess: Option[List[GroupAccess]],
)

object AclJsonProtocol extends DefaultJsonProtocol {

    implicit object AclJsonFormat extends JsonFormat[Acl] {
       def read(value: JsValue) = {
           value.asJsObject.getFields("userAccess", "groupAccess") match {
               case Seq(userAccess, groupAccess) => 
                new Acl(userAccess, groupAccess)
               case _ => throw new DeserializationException("ACL expected")
           } 
       }

        def write(acl: Acl) = JsObject(
            "userAccess" -> JsArray(acl.userAccess),  
            "groupAccess"  -> JsArray(acl.groupAccess)
        )
    }

}