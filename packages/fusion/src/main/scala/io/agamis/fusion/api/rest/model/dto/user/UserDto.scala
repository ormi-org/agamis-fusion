package io.agamis.fusion.api.rest.model.dto.user

import java.util.UUID
import java.time.Instant

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.agamis.fusion.api.rest.model.dto.common.JsonFormatters._
import spray.json._
import io.agamis.fusion.api.rest.model.dto.profile.ProfileDto
import io.agamis.fusion.api.rest.model.dto.ModelDto
import io.agamis.fusion.core.db.models.sql.User

/** User DTO with JSON support
  *
  * @param id
  * @param username
  * @param password
  * @param profiles
  * @param createdAt
  * @param updatedAt
  */
final case class UserDto(
    id: Option[UUID],
    username: String,
    profiles: Option[List[ProfileDto]],
    createdAt: Option[Instant],
    updatedAt: Option[Instant]
) extends ModelDto

object UserDto {

    /** Convert User entity to UserDto
      *
      * @param user
      */
    def from(user: User): UserDto = {
        UserDto(
          Some(user.id),
          user.username,
          Some(
            user.relatedProfiles
                .filter(_._1 == true)
                .map(r => ProfileDto.from(r._2))
          ),
          Some(user.createdAt.toInstant),
          Some(user.updatedAt.toInstant)
        )
    }
}

trait UserJsonSupport
    extends SprayJsonSupport
    with UserMutationJsonSupport
    with DefaultJsonProtocol {
    import io.agamis.fusion.api.rest.model.dto.profile.ProfileJsonProtocol._

    implicit val userFormat: RootJsonFormat[UserDto] = jsonFormat5(
      UserDto.apply
    )
}

object UserJsonProtocol extends UserJsonSupport
