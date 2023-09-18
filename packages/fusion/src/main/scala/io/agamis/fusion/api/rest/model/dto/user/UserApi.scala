package io.agamis.fusion.api.rest.model.dto.user

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.agamis.fusion.api.rest.model.dto.common.ApiStatus
import io.agamis.fusion.api.rest.model.dto.user.UserJsonSupport
import spray.json._
import io.agamis.fusion.api.rest.model.dto.common.ApiResponse

sealed trait UserApiResponse extends ApiResponse {
    def result: Any
    def status: ApiStatus
}

final case class SingleUserResponse(
    result: Option[UserDto],
    status: ApiStatus
) extends UserApiResponse

final case class UserQueryResponse(
    result: List[UserDto],
    status: ApiStatus
) extends UserApiResponse

trait UserApiJsonSupport
    extends SprayJsonSupport
    with DefaultJsonProtocol
    with UserJsonSupport {
    import io.agamis.fusion.api.rest.model.dto.common.ApiStatusJsonProtocol._

    implicit val suResponseFormat: RootJsonFormat[SingleUserResponse] =
        jsonFormat2(SingleUserResponse)
    implicit val uqResponseFormat: RootJsonFormat[UserQueryResponse] =
        jsonFormat2(UserQueryResponse)
}

object UserApiJsonProtocol extends UserApiJsonSupport
