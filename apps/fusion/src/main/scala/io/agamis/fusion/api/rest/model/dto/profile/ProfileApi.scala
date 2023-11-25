package io.agamis.fusion.api.rest.model.dto.permission

import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.agamis.fusion.api.rest.model.dto.common.ApiResponse
import io.agamis.fusion.api.rest.model.dto.common.ApiStatus
import spray.json.DefaultJsonProtocol
import spray.json.RootJsonFormat

sealed trait ProfileApiResponse extends ApiResponse {
    def result: Any
    def status: ApiStatus
}

final case class SingleProfileResponse(
    result: Option[ProfileDto],
    status: ApiStatus
) extends ProfileApiResponse

final case class MultiProfileResponse(
    result: List[ProfileDto],
    status: ApiStatus
) extends ProfileApiResponse

final case class ProfileErrorResponse(
    status: ApiStatus
) extends ApiResponse

trait ProfileApiJsonSupport
    extends SprayJsonSupport
    with DefaultJsonProtocol
    with ProfileJsonSupport {
    import io.agamis.fusion.api.rest.model.dto.common.ApiStatusJsonProtocol._

    implicit val sResponseFormat: RootJsonFormat[SingleProfileResponse] =
        jsonFormat2(SingleProfileResponse)
    implicit val mResponseFormat: RootJsonFormat[MultiProfileResponse] =
        jsonFormat2(MultiProfileResponse)

    implicit val errReponseFormat: RootJsonFormat[ProfileErrorResponse] =
        jsonFormat1(ProfileErrorResponse)
}

object ProfileApiJsonProtocol extends ProfileApiJsonSupport
