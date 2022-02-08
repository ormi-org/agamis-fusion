package io.agamis.fusion.external.api.rest.dto.filesystem

import java.util.UUID
import io.agamis.fusion.external.api.rest.dto.organization.OrganizationDto
import io.agamis.fusion.external.api.rest.dto.application.ApplicationDto

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.agamis.fusion.external.api.rest.dto.common.JsonFormatters._
import spray.json.DefaultJsonProtocol
import spray.json.JsonFormat
import spray.json.JsObject
import spray.json.JsArray
import spray.json.DeserializationException
import spray.json.JsValue
import spray.json.JsBoolean

final case class FileSystemDto(
    id: Option[UUID],
    rootdirId: String,
    label: String,
    shared: Boolean,
    organizations: List[(Boolean, OrganizationDto)],
    licensedApplications: List[ApplicationDto]
)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  import io.agamis.fusion.external.api.rest.dto.application.ApplicationJsonProtocol._
  import io.agamis.fusion.external.api.rest.dto.organization.OrganizationJsonProtocol._

  implicit object OranizationWithFileSystemRelationFormat
      extends JsonFormat[(Boolean, OrganizationDto)] {
    def write(relation: (Boolean, OrganizationDto)): JsArray = {
      JsArray(
        Vector(
          JsBoolean(relation._1),
          relation._2.asInstanceOf[JsObject]
        )
      )
    }

    def read(value: JsValue): (Boolean, OrganizationDto) = {
      value match {
        case JsArray(elements) => {
          (
            elements(0).convertTo[Boolean],
            elements(1) match {
              case org: JsObject =>
                org.convertTo[OrganizationDto]
              case _ =>
                throw new DeserializationException(
                  "Expected organization"
                )
            }
          )
        }
        case _ => throw DeserializationException("Expected tuple")
      }
    }
  }

  implicit val filesystemFormat: JsonFormat[FileSystemDto] = jsonFormat6(FileSystemDto)
}

object FileSystemJsonProtocol extends JsonSupport
