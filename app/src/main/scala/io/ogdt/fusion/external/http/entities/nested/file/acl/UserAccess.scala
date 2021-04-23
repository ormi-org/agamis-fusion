package io.ogdt.fusion.external.http.entities.nested.file.acl

import java.util.UUID

import io.ogdt.fusion.external.http.entities.nested.file.acl.access.Rights
import io.ogdt.fusion.external.http.entities.common.JsonFormatters

import spray.json.{DefaultJsonProtocol, JsonFormat, JsObject,JsString, JsValue, JsArray, DeserializationException}

final case class UserAccess(
    userId: UUID,
    rights: Rights
)

object UserJsonProtocol extends DefaultJsonProtocol{
    implicit object UserJsonFormat extends JsonFormat[UserAccess] {

       def read(value: JsValue) = {
           value.asJsObject.getFields("userId", "rights") match {
               case Seq(userId, rights) => 
                new UserAccess(UUID.fromString(userId.toString()), rights)
               case _ => throw new DeserializationException("UserAccess expected")
           } 
       }

        def write(u: UserAccess) = JsObject(
            "userId" -> JsString(u.userId.toString()),  
            "rights" -> JsObject(u.rights.asJsObject)
        )
    }

}