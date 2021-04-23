package io.ogdt.fusion.external.http.entities.nested.file.acl

import java.util.UUID

import io.ogdt.fusion.external.http.entities.nested.file.acl.access.Rights

import spray.json.{DefaultJsonProtocol, JsonFormat,JsString, JsObject, JsValue, DeserializationException}

final case class GroupAccess(
    groupId: UUID,
    rights: Rights
)


object GroupJsonProtocol extends DefaultJsonProtocol {

    implicit object GroupJsonFormat extends JsonFormat[GroupAccess] {
       def read(value: JsValue) = {
           value.asJsObject.getFields("groupId", "rights") match {
               case Seq(groupId, rights) => 
                new GroupAccess(UUID.fromString(groupId.toString()), rights)
               case _ => throw new DeserializationException("GroupAccess expected")
           } 
       }

        def write(g: GroupAccess) = JsObject(
            "groupId" -> JsString(g.groupId.toString()),  
            "rights"  -> g.rights
        )
    }

}