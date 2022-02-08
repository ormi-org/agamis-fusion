package io.agamis.fusion.external.api.rest.dto.application

import java.util.UUID
import io.agamis.fusion.external.api.rest.dto.organization.OrganizationDto
import io.agamis.fusion.external.api.rest.dto.filesystem.FileSystemDto
import io.agamis.fusion.external.api.rest.dto.permission.PermissionDto
import io.agamis.fusion.external.api.rest.dto.common.JsonFormatters._

import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.agamis.fusion.external.api.rest.dto.application.Application.INSTALLED
import io.agamis.fusion.external.api.rest.dto.application.Application.NOT_INSTALLED
import io.agamis.fusion.external.api.rest.dto.application.OrganizationApplication.ENABLED
import io.agamis.fusion.external.api.rest.dto.application.OrganizationApplication.DISABLED

case object Application {
  sealed trait Status
  case object INSTALLED extends Status
  case object NOT_INSTALLED extends Status
}

case object OrganizationApplication {
  sealed trait Status
  case object ENABLED extends Status
  case object DISABLED extends Status
}

final case class ApplicationDto(
  id: Option[UUID],
  appUniversalId: String,
  label: String,
  version: String,
  status: Application.Status,
  manifestUrl: String,
  storeUrl: String,
  organizations: List[(OrganizationApplication.Status, OrganizationDto, (FileSystemDto, String))],
  relatedPermissions: List[PermissionDto]
)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  import io.agamis.fusion.external.api.rest.dto.organization.OrganizationJsonProtocol._
  import io.agamis.fusion.external.api.rest.dto.filesystem.FileSystemJsonProtocol._
  import io.agamis.fusion.external.api.rest.dto.permission.PermissionJsonProtocol._

  implicit object ApplicationStatusFormat extends JsonFormat[Application.Status] {
    def write(status: Application.Status): JsString = {
      status match {
        case INSTALLED     => JsString("INSTALLED")
        case NOT_INSTALLED => JsString("NOT_INSTALLED")
      }
    }

    def read(value: JsValue): Application.Status = {
      value match {
        case JsString(statusString) => {
          statusString match {
            case "INSTALLED"     => Application.INSTALLED
            case "NOT_INSTALLED" => Application.NOT_INSTALLED
          }
        }
        case _                => throw DeserializationException("Expected tuple")
      }
    }
  }

  implicit object ApplicationOrganizationRelationFormat extends JsonFormat[(OrganizationApplication.Status, OrganizationDto, (FileSystemDto, String))] {
    def write(relation: (OrganizationApplication.Status, OrganizationDto, (FileSystemDto, String))): JsArray = {
      JsArray(Vector(
        JsString(
          relation._1 match {
            case ENABLED  => "ENABLED"
            case DISABLED => "DISABLED"
          }
        ),
        relation._2.asInstanceOf[JsObject],
        JsArray(Vector(
          relation._3._1.asInstanceOf[JsObject],
          JsString(relation._3._2)
        ))
      ))
    }

    def read(value: JsValue): (OrganizationApplication.Status, OrganizationDto, (FileSystemDto, String)) = {
      value match {
        case JsArray(elements) => {
          try {
            (
              elements(0) match {
                case JsString(status) => status match {
                  case "ENABLED"  => OrganizationApplication.ENABLED
                  case "DISABLED" => OrganizationApplication.DISABLED
                }
                case _ => throw DeserializationException("Expected tuple")
              },
              elements(1) match {
                case org: JsObject => 
                  org.convertTo[OrganizationDto]
                case _ =>
                  throw DeserializationException(
                    "Expected organization"
                  )
              },
              elements(2) match {
                case JsArray(elements) => {
                  (
                    elements(0) match {
                      case fs: JsObject =>
                        fs.convertTo[FileSystemDto]
                      case _ =>
                        throw DeserializationException(
                          "Expected filesystem"
                        )
                    },
                    elements(1) match {
                      case JsString(value) => value
                      case _ =>
                        throw DeserializationException(
                          "Expected string"
                        )
                    }
                  )
                }
                case _ => throw DeserializationException(
                  "Expected "
                )
              }
            )
          } catch {
            case e: Throwable => throw e
          }
        }
        case _                => throw DeserializationException("Expected tuple")
      }
    }
  }
  
  implicit val applicationFormat: JsonFormat[ApplicationDto] = jsonFormat9(ApplicationDto)
}

object ApplicationJsonProtocol extends JsonSupport