package io.agamis.fusion.external.http.entities

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat, JsValue, JsString, DeserializationException}

import io.agamis.fusion.external.http.entities.common.JsonFormatters._

final case class Profile(
    id: Option[UUID], 
    lastName: String, 
    firstName: String, 
    email: String, 
    lastLogin: String, 
    userId: Option[UUID], 
)

trait ProfileJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {
    import io.agamis.fusion.external.http.actors.ProfileRepository._

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

    implicit val profileFormat = jsonFormat6(Profile)
}