package io.agamis.fusion.api.rest.model.dto.permission

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.agamis.fusion.api.rest.model.dto.common.ApiResponse
import io.agamis.fusion.api.rest.model.dto.common.ApiStatus
import spray.json.DefaultJsonProtocol
import spray.json.RootJsonFormat

sealed trait PermissionApiResponse extends ApiResponse {
    def result: Any
    def status: ApiStatus
}

final case class SinglePermissionResponse(
    result: Option[PermissionDto],
    status: ApiStatus
) extends PermissionApiResponse

final case class MultiPermissionResponse(
    result: List[PermissionDto],
    status: ApiStatus
) extends PermissionApiResponse

final case class PermissionErrorResponse(
    status: ApiStatus
) extends ApiResponse

trait PermissionApiJsonSupport
    extends SprayJsonSupport
    with DefaultJsonProtocol
    with PermissionJsonSupport {
    import io.agamis.fusion.api.rest.model.dto.common.ApiStatusJsonProtocol._

    implicit val sResponseFormat: RootJsonFormat[SinglePermissionResponse] =
        jsonFormat2(SinglePermissionResponse)
    implicit val mResponseFormat: RootJsonFormat[MultiPermissionResponse] =
        jsonFormat2(MultiPermissionResponse)

    implicit val errReponseFormat: RootJsonFormat[PermissionErrorResponse] =
        jsonFormat1(PermissionErrorResponse)
}

object PermissionApiJsonProtocol extends PermissionApiJsonSupport
