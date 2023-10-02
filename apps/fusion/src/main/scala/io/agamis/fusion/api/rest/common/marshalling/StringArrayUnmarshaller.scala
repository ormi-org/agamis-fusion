package io.agamis.fusion.api.rest.common.marshalling

import akka.http.scaladsl.unmarshalling.Unmarshaller
import io.agamis.fusion.api.rest.common.marshalling.params.StringArrayParameter
import scala.concurrent.Future

object StringArrayUnmarshaller {
    def commaSeparatedUnmarshaller: Unmarshaller[String, List[String]] = {
        new StringArrayParameter().withMaterializer((_) => (_) => (param) => {
            Future.successful(param.split(",").toList)
        })
    }
}