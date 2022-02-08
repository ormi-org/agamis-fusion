package io.agamis.fusion.external.api.rest.dto.organizationtype

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

import io.agamis.fusion.external.api.rest.dto.common.JsonFormatters._

final case class OrganizationDto(
    id: Option[UUID],
    label: String,
    queryable: String,
    createdAt: String,
    updatedAt: String 
)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val organizationFormat: JsonFormat[OrganizationDto] = jsonFormat5(OrganizationDto)
}

object OrganizationTypeJsonProtocol extends JsonSupport