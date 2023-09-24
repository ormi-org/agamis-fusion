package io.agamis.fusion.api.rest.model.dto.user

import java.util.UUID
import java.time.Instant

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.agamis.fusion.api.rest.model.dto.common.JsonFormatters._
import spray.json._
import io.agamis.fusion.core.model.User

/** User DTO with JSON support
  *
  * @param id
  * @param username
  * @param password
  * @param createdAt
  * @param updatedAt
  */
final case class UserDto(
    id: Option[UUID],
    username: String,
    createdAt: Option[Instant],
    updatedAt: Option[Instant]
)

object UserDto {

    /** Convert User entity to UserDto
      *
      * @param user
      */
    def from(user: User): UserDto = {
        UserDto(
          Some(user.id),
          user.username,
          Some(user.createdAt.toInstant),
          Some(user.updatedAt.toInstant)
        )
    }
}

trait UserJsonSupport
    extends SprayJsonSupport
    with UserMutationJsonSupport
    with DefaultJsonProtocol {

    implicit val userFormat: RootJsonFormat[UserDto] = jsonFormat4(
      UserDto.apply
    )
}

object UserJsonProtocol extends UserJsonSupport
