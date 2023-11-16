package io.agamis.fusion.api.rest.model.dto.user

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.agamis.fusion.api.rest.model.dto.common.JsonFormatters._
import io.agamis.fusion.core.model.User
import spray.json._

import java.time.LocalDateTime
import java.util.UUID

/** User DTO with JSON support
  *
  * @param id
  * @param username
  * @param password
  * @param createdAt
  * @param updatedAt
  */
final case class UserDto(
    id: UUID,
    username: String,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime
)

object UserDto {

    /** Convert User entity to UserDto
      *
      * @param user
      */
    def from(user: User): UserDto = {
        UserDto(
          user.id,
          user.username,
          user.createdAt,
          user.updatedAt
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
