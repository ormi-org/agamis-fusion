package io.agamis.fusion.external.api.rest.common.marshalling.params

import scala.concurrent.ExecutionContext
import akka.http.scaladsl.unmarshalling.Unmarshaller
import scala.concurrent.Future
import akka.stream.Materializer
import akka.http.scaladsl.util.FastFuture
import scala.util.control.NonFatal

trait ArrayParameter[A, B] {
    def apply[A, B](f: ExecutionContext => A => Future[B]): Unmarshaller[A, B] = withMaterializer(ec => _ => f(ec))
    def withMaterializer[A, B](f: ExecutionContext => Materializer => A => Future[B]): Unmarshaller[A, B] = {
        new Unmarshaller[A, B] {
            def apply(a: A)(implicit ec: ExecutionContext, materializer: Materializer) = {
                try f(ec)(materializer)(a)
                catch { case NonFatal(e) => FastFuture.failed(e) }
            }
        }
    }
}
