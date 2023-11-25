package io.agamis.fusion.api.rest.model.dto.common

import org.apache.pekko.http.scaladsl.model.StatusCode
import org.apache.pekko.http.scaladsl.model.StatusCodes
import reactivemongo.api.bson.BSONObjectID
import spray.json.DeserializationException
import spray.json.JsString
import spray.json.JsValue
import spray.json.JsonFormat

import java.time.LocalDateTime
import java.util.UUID

object JsonFormatters {
    implicit object UUIDFormat extends JsonFormat[UUID] {
        def write(uuid: UUID): JsValue = JsString(uuid.toString)
        def read(value: JsValue): UUID = {
            value match {
                case JsString(uuid) => UUID.fromString(uuid)
                case _ =>
                    throw DeserializationException(
                      "Expected hexadecimal UUID string"
                    )
            }
        }
    }

    implicit object BSONObjectIDFormat extends JsonFormat[BSONObjectID] {
        def write(objectid: BSONObjectID): JsValue = JsString(objectid.toString)
        def read(value: JsValue): BSONObjectID = {
            value match {
                case JsString(objectid) => BSONObjectID.parse(objectid).get
                case _ =>
                    throw DeserializationException(
                      "Expected alphanumeric BSONObjectID string"
                    )
            }
        }
    }

    implicit object LocalDateTimeFormat extends JsonFormat[LocalDateTime] {
        def write(date: LocalDateTime): JsValue = JsString(date.toString)
        def read(value: JsValue): LocalDateTime = {
            value match {
                case JsString(date) => LocalDateTime.parse(date)
                case _ =>
                    throw DeserializationException("Expected ISO Date string")
            }
        }
    }

    implicit object StatusCodeFormat extends JsonFormat[StatusCode] {
        def write(code: StatusCode): JsValue = JsString(code.intValue.toString)

        def read(value: JsValue): StatusCode = {
            value match {
                case JsString(code) => {
                    StatusCodes.getForKey(code.toInt) match {
                        case Some(value) => return value
                        case None =>
                            throw DeserializationException(
                              "Expected a valid HTTP code"
                            )
                    }
                }
                case _ =>
                    throw DeserializationException("Expected a valid HTTP code")
            }
        }
    }
}
