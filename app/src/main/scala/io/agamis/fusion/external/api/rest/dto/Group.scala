package io.agamis.fusion.external.api.rest.dto

import java.util.UUID
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat, JsValue, JsString, DeserializationException}

import io.agamis.fusion.external.api.rest.dto.common.JsonFormatters._

final case class GroupDto (
    id: UUID, 
    name: String,
    members: List[ProfileDto],
)

trait GroupDtoJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {

    implicit val groupFormat = jsonFormat2(GroupDto)
}