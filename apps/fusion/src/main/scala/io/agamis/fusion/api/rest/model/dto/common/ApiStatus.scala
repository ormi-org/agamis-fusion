package io.agamis.fusion.api.rest.model.dto.common

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import spray.json.RootJsonFormat

final case class ApiStatus(
    code: Option[String],
    message: Option[String]
)

trait ApiStatusJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

    implicit val apiStatusFormat: RootJsonFormat[ApiStatus] = jsonFormat2(
      ApiStatus.apply
    )
}

object ApiStatusJsonProtocol extends ApiStatusJsonSupport
