package io.ogdt.fusion.external.http.entities.common

import java.util.UUID
import spray.json.{JsString, JsValue, DeserializationException, JsonFormat}
import reactivemongo.api.bson.BSONObjectID

object JsonFormatters {
    implicit object UUIDFormat extends JsonFormat[UUID] {
        def write(uuid: UUID) = JsString(uuid.toString)
        def read(value: JsValue) = {
            value match {
                case JsString(uuid) => UUID.fromString(uuid)
                case _              => throw new DeserializationException("Expected hexadecimal UUID string")
            }
        }
    }

    implicit object BSONObjectIDFormat extends JsonFormat[BSONObjectID] {
        def write(objectid: BSONObjectID) = JsString(objectid.toString)
        def read(value: JsValue) = {
            value match {
                case JsString(objectid) => BSONObjectID.parse(objectid).get
                case _                  => throw new DeserializationException("Expected alphanumeric BSONObjectID string")
            }
        }
    }
}