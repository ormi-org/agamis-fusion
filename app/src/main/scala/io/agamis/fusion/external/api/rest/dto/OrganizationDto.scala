package io.agamis.fusion.external.api.rest.dto.organization

import java.util.UUID

import io.agamis.fusion.external.api.rest.dto.common.JsonFormatters._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

final case class OrganizationDto(
    id: Option[UUID], 
    label: String, 
    queryable: String, 
    createdAt: String, 
    updatedAt: String 
)

trait OrganizationJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val organizationFormat: RootJsonFormat[OrganizationDto] = jsonFormat5(OrganizationDto)
}

object OrganizationJsonProtocol extends OrganizationJsonSupport