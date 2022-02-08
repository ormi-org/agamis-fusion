package io.agamis.fusion.external.api.rest.dto.user

import java.util.UUID
import java.time.Instant

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.agamis.fusion.external.api.rest.dto.common.JsonFormatters._
import spray.json._
import io.agamis.fusion.external.api.rest.dto.profile.ProfileDto

/**
  * User DTO with JSON support
  *
  * @param id
  * @param username
  * @param password
  * 
  * @param createdAt
  * @param updatedAt
  */
final case class UserDto (
    id: Option[UUID],
    username: String,
    password: String,
    profiles: List[ProfileDto],
    createdAt: Option[Instant],
    updatedAt: Option[Instant]
)

trait UserJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
    import io.agamis.fusion.external.api.rest.dto.profile.ProfileJsonProtocol._

    implicit val userFormat: RootJsonFormat[UserDto] = jsonFormat6(UserDto)
}

object UserJsonProtocol extends UserJsonSupport