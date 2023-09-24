package io.agamis.fusion.api.rest.model.dto.organization

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.agamis.fusion.api.rest.model.dto.common.JsonFormatters._
import io.agamis.fusion.core.model.Organization
import spray.json._
import io.agamis.fusion.api.rest.model.dto.common.ModelDto

import java.time.Instant
import java.util.UUID

/** Organization DTO with JSON
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
final case class OrganizationDto(
    id: Option[UUID],
    label: String,
    queryable: Boolean,
    createdAt: Option[Instant],
    updatedAt: Option[Instant]
) extends ModelDto

object OrganizationDto {
    def from(o: Organization): OrganizationDto = {
        OrganizationDto(
          Some(o.id),
          o.label,
          o.queryable,
          Some(o.createdAt.toInstant),
          Some(o.updatedAt.toInstant)
        )
    }
}

trait OrganizationJsonSupport
    extends SprayJsonSupport
    with DefaultJsonProtocol {

    implicit val organizationFormat: RootJsonFormat[OrganizationDto] =
        jsonFormat5(OrganizationDto.apply)
}

object OrganizationJsonProtocol extends OrganizationJsonSupport
