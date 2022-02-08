package io.agamis.fusion.external.api.rest.dto.profile

import java.time.Instant
import java.util.UUID

import io.agamis.fusion.external.api.rest.dto.common.JsonFormatters._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._
import io.agamis.fusion.external.api.rest.dto.permission.PermissionDto
import io.agamis.fusion.external.api.rest.dto.organization.OrganizationDto

/**
  * Profile DTO with JSON support
  *
  * @param id
  * @param lastName
  * @param firstName
  * @param mainEmail
  * @param emails
  * @param permissions
  * @param organization
  * @param lastLogin
  * @param userId
  * @param createdAt
  * @param updatedAt
  */
final case class ProfileDto(
    id: Option[UUID],
    lastName: String,
    firstName: String,
    mainEmail: String,
    emails: List[String],
    permissions: Option[List[PermissionDto]],
    organization: Option[OrganizationDto],
    lastLogin: Instant,
    userId: Option[String],
    createdAt: Option[Instant],
    updatedAt: Option[Instant]
)

trait ProfileJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
    import io.agamis.fusion.external.api.rest.dto.permission.PermissionJsonProtocol._
    import io.agamis.fusion.external.api.rest.dto.organization.OrganizationJsonProtocol._

    implicit val profileFormat: RootJsonFormat[ProfileDto] = jsonFormat11(ProfileDto)
}

object ProfileJsonProtocol extends ProfileJsonSupport
