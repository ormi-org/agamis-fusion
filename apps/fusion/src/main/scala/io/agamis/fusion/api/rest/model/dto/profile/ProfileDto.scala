package io.agamis.fusion.api.rest.model.dto.permission

import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.agamis.fusion.api.rest.model.dto.common.JsonFormatters._
import io.agamis.fusion.api.rest.model.dto.common.ModelDto
import io.agamis.fusion.core.model.Profile
import spray.json._

import java.time.LocalDateTime
import java.util.UUID

/** Profile DTO with JSON
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
final case class ProfileDto(
    id: UUID,
    firstname: String,
    lastname: String,
    lastLogin: LocalDateTime,
    isActive: Boolean,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime
) extends ModelDto

object ProfileDto {
    def from(p: Profile): ProfileDto = {
        ProfileDto(
          p.id,
          p.firstname,
          p.lastname,
          p.lastLogin,
          p.isActive,
          p.createdAt,
          p.updatedAt
        )
    }
}

trait ProfileJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

    implicit val organizationFormat: RootJsonFormat[ProfileDto] =
        jsonFormat7(ProfileDto.apply)
}

object ProfileJsonProtocol extends ProfileJsonSupport
