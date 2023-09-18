package io.agamis.fusion.api.rest.model.dto.organizationtype

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.agamis.fusion.api.rest.model.dto.common.JsonFormatters._
import io.agamis.fusion.api.rest.model.dto.common.LanguageMapping
import io.agamis.fusion.api.rest.model.dto.organization.OrganizationDto
import io.agamis.fusion.core.db.models.sql.OrganizationType
import spray.json._

import java.time.Instant
import java.util.UUID
import scala.collection.mutable.ListBuffer

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
    labels: List[LanguageMapping],
    relatedOrganizations: Option[List[OrganizationDto]],
    createdAt: Option[Instant],
    updatedAt: Option[Instant]
)

object OrganizationTypeDto {
    def from(o: OrganizationType): OrganizationTypeDto = {
        apply(
          Some(o.id),
          o.labels
              .foldLeft(ListBuffer.empty[LanguageMapping]) { (acc, i) =>
                  {
                      acc += new LanguageMapping(
                        i._1._1,
                        i._1._2,
                        i._2._1,
                        i._2._2
                      )
                  }
              }
              .toList,
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
        labels: List[LanguageMapping],
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

trait OrganizationTypeJsonSupport
    extends SprayJsonSupport
    with DefaultJsonProtocol {
    import io.agamis.fusion.api.rest.model.dto.organization.OrganizationJsonProtocol.organizationFormat
    import io.agamis.fusion.api.rest.model.dto.common.LanguageMappingJsonProtocol.languageMappingFormat

    implicit val organizationTypeFormat: RootJsonFormat[OrganizationTypeDto] =
        jsonFormat5(OrganizationTypeDto.apply)
}

object OrganizationTypeJsonProtocol extends OrganizationTypeJsonSupport
