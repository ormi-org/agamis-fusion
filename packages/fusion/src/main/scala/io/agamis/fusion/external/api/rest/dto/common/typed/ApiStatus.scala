package io.agamis.fusion.external.api.rest.dto.common.typed

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import spray.json.RootJsonFormat
import akka.http.scaladsl.model.StatusCode

final case class ApiStatus (
  code: StatusCode,
  message: Option[String]
)

trait ApiStatusJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  import io.agamis.fusion.external.api.rest.dto.common.JsonFormatters._

  implicit val apiStatusFormat: RootJsonFormat[ApiStatus] = jsonFormat2(ApiStatus.apply)
}

object ApiStatusJsonProtocol extends ApiStatusJsonSupport