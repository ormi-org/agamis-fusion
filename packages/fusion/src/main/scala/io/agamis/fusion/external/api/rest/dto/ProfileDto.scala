package io.agamis.fusion.external.api.rest.dto.profile

import java.time.Instant
import java.util.UUID
import io.agamis.fusion.external.api.rest.dto.permission.PermissionDto
import io.agamis.fusion.external.api.rest.dto.organization.OrganizationDto
import io.agamis.fusion.external.api.rest.dto.user.UserDto

import io.agamis.fusion.external.api.rest.dto.common.JsonFormatters._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._
import io.agamis.fusion.core.db.models.sql.Profile
import io.agamis.fusion.core.db.models.sql.Organization
import io.agamis.fusion.core.db.models.sql.User
import io.agamis.fusion.core.db.models.sql.generics.Email

/**
  * Profile DTO with JSON support
  *
  * @param id
  * @param lastName
  * @param firstName
  * @param mainEmail
  * @param emails (includable) other emails
  * @param permissions (includable) permissions
  * @param organization (includable) organization
  * @param lastLogin
  * @param userId
  * @param user (includable) related user
  * @param createdAt
  * @param updatedAt
  */
final case class ProfileDto(
    id: Option[UUID],
    alias: Option[String],
    lastName: String,
    firstName: String,
    mainEmail: String,
    emails: Option[List[String]],
    permissions: Option[List[PermissionDto]],
    organization: Option[OrganizationDto],
    isActive: Boolean,
    lastLogin: Instant,
    userId: String,
    user: Option[UserDto],
    createdAt: Option[Instant],
    updatedAt: Option[Instant]
)

object ProfileDto {
  def from(p: Profile): ProfileDto = {
    ProfileDto(
      Some(p.id),
      p.alias,
      p.lastname,
      p.firstname,
      p.mainEmail.address,
      Some(p.emails.filter(_._1 == true).map(_._2.address)),
      Some(p.permissions.filter(_._1 == true).map(r => PermissionDto.from(r._2))),
      p.relatedOrganization.collect { case o: Organization => OrganizationDto.from(o) },
      p.isActive,
      p.lastLogin.toInstant,
      p.userId.toString,
      p.relatedUser.collect { case u: User => UserDto.from(u) },
      Some(p.createdAt.toInstant),
      Some(p.updatedAt.toInstant)
    )
  }
}

trait ProfileJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
    import io.agamis.fusion.external.api.rest.dto.permission.PermissionJsonProtocol._
    import io.agamis.fusion.external.api.rest.dto.organization.OrganizationJsonProtocol._
    import io.agamis.fusion.external.api.rest.dto.user.UserApiJsonProtocol._

    implicit val profileFormat: RootJsonFormat[ProfileDto] = jsonFormat14(ProfileDto.apply)
}

object ProfileJsonProtocol extends ProfileJsonSupport
