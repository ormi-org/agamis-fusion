package io.ogdt.fusion.external.http.entities

import java.util.UUID
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat, JsValue, JsString, DeserializationException}

import io.ogdt.fusion.external.http.entities.common.JsonFormatters._

import io.ogdt.fusion.external.http.authorization.JwtAuthorization

final case class User (
    id: UUID,
    username: String,
    password: String
) 

trait UserJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {
    import io.ogdt.fusion.external.http.actors.UserRepository._

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

    implicit val userFormat = jsonFormat3(User)
}