package io.agamis.fusion.api.rest.model.dto.group

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.agamis.fusion.core.db.models.sql.Group
import io.agamis.fusion.api.rest.model.dto.common.JsonFormatters._
import io.agamis.fusion.api.rest.model.dto.organization.OrganizationDto
import io.agamis.fusion.api.rest.model.dto.permission.PermissionDto
import io.agamis.fusion.api.rest.model.dto.profile.ProfileDto
import spray.json._

import java.time.Instant
import java.util.UUID

final case class GroupDto(
  id: Option[UUID],
  name: String,
  members: Option[List[ProfileDto]],
  permissions: Option[List[PermissionDto]],
  relatedOrganization: Option[OrganizationDto],
  createdAt: Option[Instant],
  updatedAt: Option[Instant]
)

object GroupDto {
  def from(g: Group): GroupDto = {
    apply(
      Some(g.id),
      g.name,
      Some(g.members.filter(_._1 == true).map(r => ProfileDto.from(r._2))),
      Some(g.permissions.filter(_._1 == true).map(r => PermissionDto.from(r._2))),
      Some(OrganizationDto.from(g.relatedOrganization.orNull)),
      Some(g.createdAt.toInstant),
      Some(g.updatedAt.toInstant)
    )
  }

  def apply(
    id: Option[UUID],
    name: String,
    members: Option[List[ProfileDto]],
    permissions: Option[List[PermissionDto]],
    relatedOrganization: Option[OrganizationDto],
    createdAt: Option[Instant],
    updatedAt: Option[Instant]
  ): GroupDto = {
    GroupDto(
      id,
      name,
      members,
      permissions,
      relatedOrganization,
      createdAt,
      updatedAt
    )
  }
}

trait GroupJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  import io.agamis.fusion.api.rest.model.dto.profile.ProfileJsonProtocol._
  import io.agamis.fusion.api.rest.model.dto.permission.PermissionJsonProtocol._
  import io.agamis.fusion.api.rest.model.dto.organization.OrganizationJsonProtocol._

  implicit val groupFormat: RootJsonFormat[GroupDto] = jsonFormat7(GroupDto.apply)
}

object GroupJsonProtocol extends GroupJsonSupport
