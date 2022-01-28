package io.agamis.fusion.external.api.rest.dto

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat, JsValue, JsString, DeserializationException}

import io.agamis.fusion.external.api.rest.dto.common.JsonFormatters._

final case class Organization(
    id: Option[UUID], 
    label: String, 
    queryable: String, 
    createdAt: String, 
    updatedAt: String 
)

trait OrganizationJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {
    import io.agamis.fusion.external.api.rest.actors.OrganizationRepository._

    implicit object StatusFormat extends RootJsonFormat[Status] {
        def write(status: Status): JsValue = status match {
            case Failed     => JsString("Failed")
            case Successful => JsString("Success") 
    }

        def read(json: JsValue): Status = json match {
            case JsString("Failed")     => Failed
            case JsString("Successful") => Successful
            case _                      => throw new DeserializationException("Status unexpected")
        }
    }

    implicit val organizationFormat = jsonFormat5(Organization)
}