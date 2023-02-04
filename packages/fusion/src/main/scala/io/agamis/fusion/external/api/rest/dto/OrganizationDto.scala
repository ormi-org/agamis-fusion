package io.agamis.fusion.external.api.rest.dto.organization

import io.agamis.fusion.core.db.models.sql.Organization
import io.agamis.fusion.external.api.rest.dto.group.GroupDto
import io.agamis.fusion.external.api.rest.dto.profile.ProfileDto
import io.agamis.fusion.external.api.rest.dto.filesystem.FileSystemDto
import io.agamis.fusion.external.api.rest.dto.application.ApplicationDto
import io.agamis.fusion.external.api.rest.dto.application.OrganizationApplication
import io.agamis.fusion.external.api.rest.dto.organizationtype.OrganizationTypeDto
import java.util.UUID
import java.time.Instant

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.agamis.fusion.external.api.rest.dto.common.JsonFormatters._
import spray.json._
import io.agamis.fusion.core.db.models.sql.FileSystem
import io.agamis.fusion.core.db.models.sql.OrganizationType

/**
  * Organization DTO with JSON
  *
  * @param id
  * @param label
  * @param type
  * @param queryable
  * @param profiles
  * @param groups
  * @param defaultFileSystem
  * @param fileSystems
  * @param applications
  * @param createdAt
  * @param updatedAt
  */
final case class OrganizationDto(
    id: Option[UUID],
    label: String,
    `type`: Option[OrganizationTypeDto],
    queryable: Boolean,
    profiles: Option[List[ProfileDto]],
    groups: Option[List[GroupDto]],
    defaultFileSystem: Option[FileSystemDto],
    fileSystems: Option[List[FileSystemDto]],
    applications: Option[
      List[(OrganizationApplication.Status, ApplicationDto)]
    ],
    createdAt: Option[Instant],
    updatedAt: Option[Instant]
)

object OrganizationDto {
  def from(o: Organization): OrganizationDto = {
    OrganizationDto(
      Some(o.id),
      o.label,
      o.`type`.collect { case ot: OrganizationType => OrganizationTypeDto.from(ot) },
      o.queryable,
      Some(
        o.relatedProfiles.filter(_._1 == true).map(r => ProfileDto.from(r._2))
      ),
      Some(o.relatedGroups.filter(_._1 == true).map(r => GroupDto.from(r._2))),
      o.defaultFileSystem.collect { case fs: FileSystem => FileSystemDto.from(fs) },
      Some(o.fileSystems.map(r => FileSystemDto.from(r._2))),
      Some(
        o.applications
          .filter(_._1 == true)
          .map(r =>
            (
              OrganizationApplication.Status.from(r._2._1),
              ApplicationDto.from(r._2._2)
            )
          )
      ),
      Some(o.createdAt.toInstant),
      Some(o.updatedAt.toInstant)
    )
  }
}

trait OrganizationJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  import io.agamis.fusion.external.api.rest.dto.profile.ProfileJsonProtocol._
  import io.agamis.fusion.external.api.rest.dto.organizationtype.OrganizationTypeJsonProtocol._
  import io.agamis.fusion.external.api.rest.dto.application.ApplicationJsonProtocol._
  import io.agamis.fusion.external.api.rest.dto.group.GroupJsonProtocol._
  import io.agamis.fusion.external.api.rest.dto.filesystem.FileSystemJsonProtocol._

  implicit object OrganizationApplicationRelationFormat extends JsonFormat[(OrganizationApplication.Status, ApplicationDto)] {
    def write(
      relation: (
        OrganizationApplication.Status,
        ApplicationDto
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
          relation._2.asInstanceOf[JsObject]
        )
      )
    }

    def read(value: JsValue): (
      OrganizationApplication.Status,
      ApplicationDto
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
              case app: JsObject =>
                app.convertTo[ApplicationDto]
              case _ =>
                throw DeserializationException(
                  "Expected application"
                )
            }
          )
        }
        case _ => throw DeserializationException("Expected tuple")
      }
    }
  }

  implicit val organizationFormat: RootJsonFormat[OrganizationDto] =
    jsonFormat11(OrganizationDto.apply)
}

object OrganizationJsonProtocol extends OrganizationJsonSupport
