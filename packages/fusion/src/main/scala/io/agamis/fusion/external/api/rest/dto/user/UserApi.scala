package io.agamis.fusion.external.api.rest.dto.user

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.agamis.fusion.external.api.rest.dto.common.typed.ApiStatus
import io.agamis.fusion.external.api.rest.dto.user.UserJsonSupport
import spray.json._

sealed trait UserApiResponse {
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

trait UserApiJsonSupport extends SprayJsonSupport with DefaultJsonProtocol with UserJsonSupport {
  import io.agamis.fusion.external.api.rest.dto.common.typed.ApiStatusJsonProtocol._

  implicit val suResponseFormat: RootJsonFormat[SingleUserResponse] = jsonFormat2(SingleUserResponse)
  implicit val uqResponseFormat: RootJsonFormat[UserQueryResponse] = jsonFormat2(UserQueryResponse)
}

object UserApiJsonProtocol extends UserApiJsonSupport

