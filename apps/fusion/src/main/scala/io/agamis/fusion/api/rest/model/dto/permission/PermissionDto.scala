package io.agamis.fusion.api.rest.model.dto.permission

import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.agamis.fusion.api.rest.model.dto.common.JsonFormatters._
import io.agamis.fusion.api.rest.model.dto.common.ModelDto
import io.agamis.fusion.core.model.Permission
import spray.json._

import java.time.LocalDateTime
import java.util.UUID

/** Permission DTO with JSON
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
final case class PermissionDto(
    id: UUID,
    key: String,
    editable: Boolean,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime
) extends ModelDto

object PermissionDto {
    def from(p: Permission): PermissionDto = {
        PermissionDto(
          p.id,
          p.label,
          p.queryable,
          p.createdAt,
          p.updatedAt
        )
    }
}

trait PermissionJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

    implicit val organizationFormat: RootJsonFormat[PermissionDto] =
        jsonFormat5(PermissionDto.apply)
}

object PermissionJsonProtocol extends PermissionJsonSupport
