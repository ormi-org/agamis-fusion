package io.agamis.fusion.external.api.rest.dto.organizationtype

import io.agamis.fusion.core.db.models.sql.OrganizationType
import io.agamis.fusion.external.api.rest.dto.organization.OrganizationDto
import java.util.UUID
import java.time.Instant

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.agamis.fusion.external.api.rest.dto.common.JsonFormatters._
import spray.json._

/** OrganizationType DTO with JSON support
  *
  * @param id
  * @param label
  * @param relatedOrganizations
  * @param createdAt
  * @param updatedAt
  */
final case class OrganizationTypeDto(
  id: Option[UUID],
  label: Map[(UUID, UUID), (String, String)],
  relatedOrganizations: Option[List[OrganizationDto]],
  createdAt: Option[Instant],
  updatedAt: Option[Instant]
)

object OrganizationTypeDto {
  def from(o: OrganizationType): OrganizationTypeDto = {
    apply(
      Some(o.id),
      o.labels,
      Some(
        o.relatedOrganizations
          .filter(_._1 == true)
          .map(r => OrganizationDto.from(r._2))
      ),
      Some(o.createdAt.toInstant),
      Some(o.updatedAt.toInstant)
    )
  }

  def apply(
    id: Option[UUID],
    labels: Map[(UUID, UUID), (String, String)],
    relatedOrganizations: Option[List[OrganizationDto]],
    createdAt: Option[Instant],
    updatedAt: Option[Instant]
  ): OrganizationTypeDto = {
    OrganizationTypeDto(
      id,
      labels,
      relatedOrganizations,
      createdAt,
      updatedAt
    )
  }
}

trait OrganizationTypeJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  import io.agamis.fusion.external.api.rest.dto.organization.OrganizationJsonProtocol.organizationFormat

  implicit val organizationTypeFormat: RootJsonFormat[OrganizationTypeDto] =
    jsonFormat5(OrganizationTypeDto.apply)
}

object OrganizationTypeJsonProtocol extends OrganizationTypeJsonSupport
