package io.ogdt.fusion.external.http.entities.common

import java.util.UUID
import spray.json.{JsString, JsValue, DeserializationException, JsonFormat}
import reactivemongo.api.bson.BSONObjectID
import java.time.Instant

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

    implicit object InstantFormat extends JsonFormat[Instant] {
        def write(date: Instant) = JsString(date.toString())
        def read(value: JsValue) = {
            value match {
                case JsString(date) => Instant.parse(date)
                case _              => throw new DeserializationException("Expected ISO Date string")
            }
        }
    }
}