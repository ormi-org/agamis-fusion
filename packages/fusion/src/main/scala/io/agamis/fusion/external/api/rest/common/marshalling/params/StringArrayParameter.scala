package io.agamis.fusion.external.api.rest.common.marshalling.params

import akka.http.scaladsl.unmarshalling.Unmarshaller
import java.util.concurrent.Future
import scala.concurrent.ExecutionContext
import akka.stream.Materializer
import scala.util.control.NonFatal
import akka.http.scaladsl.util.FastFuture

class StringArrayParameter extends ArrayParameter[String, List[String]] {}
