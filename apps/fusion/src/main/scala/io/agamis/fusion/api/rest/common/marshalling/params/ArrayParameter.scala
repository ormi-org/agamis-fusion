package io.agamis.fusion.api.rest.common.marshalling.params

import org.apache.pekko.http.scaladsl.unmarshalling.Unmarshaller
import org.apache.pekko.http.scaladsl.util.FastFuture
import org.apache.pekko.stream.Materializer

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.control.NonFatal

trait ArrayParameter[A, B] {
    def apply(f: ExecutionContext => A => Future[B]): Unmarshaller[A, B] =
        withMaterializer(ec => _ => f(ec))
    def withMaterializer(
        f: ExecutionContext => Materializer => A => Future[B]
    ): Unmarshaller[A, B] = {
        new Unmarshaller[A, B] {
            def apply(
                a: A
            )(implicit ec: ExecutionContext, materializer: Materializer) = {
                try f(ec)(materializer)(a)
                catch { case NonFatal(e) => FastFuture.failed(e) }
            }
        }
    }
}
