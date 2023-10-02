package io.agamis.fusion.api.rest.model.dto.organization

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.agamis.fusion.api.rest.model.dto.common.ApiResponse
import io.agamis.fusion.api.rest.model.dto.common.ApiStatus
import spray.json.DefaultJsonProtocol
import spray.json.RootJsonFormat

sealed trait OrganizationApiResponse extends ApiResponse {
    def result: Any
    def status: ApiStatus
}

final case class SingleOrganizationResponse(
    result: Option[OrganizationDto],
    status: ApiStatus
) extends OrganizationApiResponse

final case class MultiOrganizationResponse(
    result: List[OrganizationDto],
    status: ApiStatus
) extends OrganizationApiResponse

final case class OrganizationErrorResponse(
    status: ApiStatus
) extends ApiResponse

trait OrganizationApiJsonSupport
    extends SprayJsonSupport
    with DefaultJsonProtocol
    with OrganizationJsonSupport {
    import io.agamis.fusion.api.rest.model.dto.common.ApiStatusJsonProtocol._

    implicit val soResponseFormat: RootJsonFormat[SingleOrganizationResponse] =
        jsonFormat2(SingleOrganizationResponse)
    implicit val moResponseFormat: RootJsonFormat[MultiOrganizationResponse] =
        jsonFormat2(MultiOrganizationResponse)

    implicit val errReponseFormat: RootJsonFormat[OrganizationErrorResponse] =
        jsonFormat1(OrganizationErrorResponse)
}

object OrganizationApiJsonProtocol extends OrganizationApiJsonSupport
