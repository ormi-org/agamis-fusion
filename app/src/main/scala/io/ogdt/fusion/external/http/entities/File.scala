package io.ogdt.fusion.external.http.entities

import reactivemongo.api.bson.BSONObjectID
import java.util.UUID

import io.ogdt.fusion.external.http.entities.nested.file.{Metadata, Acl}

import spray.json.DefaultJsonProtocol
import spray.json.RootJsonFormat
import spray.json.JsArray
import spray.json.JsObject

import io.ogdt.fusion.external.http.entities.common.JsonFormatters
import spray.json.{JsString, JsValue}
import spray.json.JsBoolean
import spray.json.DeserializationException

final case class File(
    id: BSONObjectID,
    name: String,
    `type`: String,
    path: Option[String],
    parent: Option[BSONObjectID],
    chunkList: Option[List[UUID]],
    metadata: Metadata,
    versioned: Option[Boolean],
    acl: Acl,
    owner: UUID
)

object FileJsonProtocol extends DefaultJsonProtocol{

    implicit object FileJsonFormat extends RootJsonFormat[File] {
        def read(value: JsValue) = {
            value.asJsObject.getFields("id", "name", "type", "path", "parent", "chunkList", "metadata","versionned", "acl", "owner") match {
                case Seq(id,JsString(name), JsString(`type`), JsString(path), JsString(parent), JsArray(chunkList), metadata, JsBoolean(versionned),acl,owner) => 
                    new File(id, name, `type`, path, parent, chunkList, metadata,versionned,acl, UUID.fromString(owner.toString()))
                case _ => throw new DeserializationException("File expected")
            }
        }
        
        def write(f: File): JsValue = JsObject(
            "id"         -> f.id, 
            "name"       -> JsString(f.name),
            "type"       -> JsString(f.`type`),
            "path"       -> JsString(f.path),
            "parent"     -> f.parent,
            "chunkList"  -> f.chunkList, 
            "metadata"   -> f.metadata, 
            "versionned" -> JsBoolean(f.versioned), 
            "acl"        -> JsArray(f.acl), 
            "owner"      -> JsString(f.owner.toString())
        ) 
    }

}