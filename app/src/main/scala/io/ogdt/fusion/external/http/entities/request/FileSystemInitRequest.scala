package io.ogdt.fusion.external.http.entities.request

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{RootJsonFormat,JsValue,JsString,DefaultJsonProtocol,DeserializationException}
import scala.util.Failure

final case class FileSystemInitRequest(
    label: String,
    shared: Boolean
)

trait FileSystemJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {
    import io.ogdt.fusion.external.http.actors.FileSystemRepository._

    implicit object StatusFormat extends RootJsonFormat[Status] {
          def write(status: Status): JsValue = status match {
            case _                      => throw new UnsupportedOperationException("UnsupportedOperationException")
        }   

        def read(json: JsValue): Status = json match {
            case JsString("Failed")     => Failed
            case JsString("Successful") => Successful
            case _                      => throw new DeserializationException("Status unexpected")
        }
    }

    implicit val filesystem = jsonFormat2(FileSystemInitRequest.apply)
}