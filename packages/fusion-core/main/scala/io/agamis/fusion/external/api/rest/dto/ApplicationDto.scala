package io.agamis.fusion.external.api.rest.dto.application

import io.agamis.fusion.external.api.rest.dto.organization.OrganizationDto
import io.agamis.fusion.external.api.rest.dto.filesystem.FileSystemDto
import io.agamis.fusion.external.api.rest.dto.permission.PermissionDto
import io.agamis.fusion.core.db.models.sql.relations
import io.agamis.fusion.core.db.models.sql
import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.agamis.fusion.external.api.rest.dto.common.JsonFormatters._
import spray.json._

case object Application {
  sealed trait Status
  object Status {
    def from(s: sql.Application.Status): Status = {
      s match {
        case sql.Application.INSTALLED     => INSTALLED
        case sql.Application.NOT_INSTALLED => NOT_INSTALLED
      }
    }
  }
  case object INSTALLED extends Status
  case object NOT_INSTALLED extends Status
}

case object OrganizationApplication {
  sealed trait Status
  object Status {
    def from(s: relations.OrganizationApplication.Status): Status = {
      s match {
        case relations.OrganizationApplication.ENABLED  => ENABLED
        case relations.OrganizationApplication.DISABLED => DISABLED
      }
    }
  }
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
    organizations: List[
      (OrganizationApplication.Status, OrganizationDto, (FileSystemDto, String))
    ],
    relatedPermissions: List[PermissionDto]
)

object ApplicationDto {
  def from(a: sql.Application): ApplicationDto = {
    apply(
      Some(a.id),
      a.appUniversalId,
      a.label,
      a.version,
      Application.Status.from(a.status),
      a.manifestUrl,
      a.storeUrl,
      a.organizations.filter(_._1 == true).map(r => {
        (
          OrganizationApplication.Status.from(r._2._1),
          OrganizationDto.from(r._2._2),
          (
            FileSystemDto.from(r._2._3._1),
            r._2._3._2
          )
        )
      }),
      a.relatedPermissions.filter(_._1 == true).map(r => PermissionDto.from(r._2))
    )
  }

  def apply(
      id: Option[UUID],
      appUniversalId: String,
      label: String,
      version: String,
      status: Application.Status,
      manifestUrl: String,
      storeUrl: String,
      organizations: List[
        (
            OrganizationApplication.Status,
            OrganizationDto,
            (FileSystemDto, String)
        )
      ],
      relatedPermissions: List[PermissionDto]
  ): ApplicationDto = {
    ApplicationDto(
      id,
      appUniversalId,
      label,
      version,
      status,
      manifestUrl,
      storeUrl,
      organizations,
      relatedPermissions
    )
  }
}

trait ApplicationJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  import io.agamis.fusion.external.api.rest.dto.organization.OrganizationJsonProtocol._
  import io.agamis.fusion.external.api.rest.dto.filesystem.FileSystemJsonProtocol._
  import io.agamis.fusion.external.api.rest.dto.permission.PermissionJsonProtocol._

  implicit object ApplicationStatusFormat
      extends JsonFormat[Application.Status] {
    def write(status: Application.Status): JsString = {
      status match {
        case Application.INSTALLED     => JsString("INSTALLED")
        case Application.NOT_INSTALLED => JsString("NOT_INSTALLED")
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
        case _ => throw DeserializationException("Expected tuple")
      }
    }
  }

  implicit object ApplicationOrganizationRelationFormat
      extends JsonFormat[
        (
            OrganizationApplication.Status,
            OrganizationDto,
            (FileSystemDto, String)
        )
      ] {
    def write(
        relation: (
            OrganizationApplication.Status,
            OrganizationDto,
            (FileSystemDto, String)
        )
    ): JsArray = {
      JsArray(
        Vector(
          JsString(
            relation._1 match {
              case OrganizationApplication.ENABLED  => "ENABLED"
              case OrganizationApplication.DISABLED => "DISABLED"
            }
          ),
          relation._2.asInstanceOf[JsObject],
          JsArray(
            Vector(
              relation._3._1.asInstanceOf[JsObject],
              JsString(relation._3._2)
            )
          )
        )
      )
    }

    def read(value: JsValue): (
        OrganizationApplication.Status,
        OrganizationDto,
        (FileSystemDto, String)
    ) = {
      value match {
        case JsArray(elements) => {
          (
            elements(0) match {
              case JsString(status) =>
                status match {
                  case "ENABLED"  => OrganizationApplication.ENABLED
                  case "DISABLED" => OrganizationApplication.DISABLED
                }
              case _ => throw DeserializationException("Expected status")
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
              case _ =>
                throw DeserializationException(
                  "Expected "
                )
            }
          )
        }
        case _ => throw DeserializationException("Expected tuple")
      }
    }
  }

  implicit val applicationFormat: RootJsonFormat[ApplicationDto] = jsonFormat9(
    ApplicationDto.apply
  )
}

object ApplicationJsonProtocol extends ApplicationJsonSupport
