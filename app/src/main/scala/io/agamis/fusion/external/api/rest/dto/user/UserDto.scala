package io.agamis.fusion.external.api.rest.dto.user

import java.util.UUID
import java.time.Instant

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.agamis.fusion.external.api.rest.dto.common.JsonFormatters._
import spray.json._
import io.agamis.fusion.external.api.rest.dto.profile.ProfileDto
import io.agamis.fusion.core.db.models.sql.User

/**
  * User DTO with JSON support
  *
  * @param id
  * @param username
  * @param password
  * @param profiles
  * @param createdAt
  * @param updatedAt
  */
final case class UserDto (
  id: Option[UUID],
  username: String,
  profiles: List[ProfileDto],
  createdAt: Option[Instant],
  updatedAt: Option[Instant]
)

object UserDto {
  /**
    * Convert User entity to UserDto
    *
    * @param user
    */
  def from(user: User): UserDto = {
    apply(
      Some(user.id),
      user.username,
      user.relatedProfiles.filter(_._1 == true).map(r => ProfileDto.from(r._2)),
      Some(user.createdAt.toInstant),
      Some(user.updatedAt.toInstant)
    )
  }

  def apply(
    id: Option[UUID],
    username: String,
    profiles: List[ProfileDto],
    createdAt: Option[Instant],
    updatedAt: Option[Instant]
  ): UserDto = {
    UserDto(
      id,
      username,
      profiles,
      createdAt,
      updatedAt
    )
  }
}

trait UserJsonSupport extends SprayJsonSupport with UserMutationJsonSupport with DefaultJsonProtocol {
    import io.agamis.fusion.external.api.rest.dto.profile.ProfileJsonProtocol._

    implicit val userFormat: RootJsonFormat[UserDto] = jsonFormat5(UserDto.apply)
}

object UserJsonProtocol extends UserJsonSupport