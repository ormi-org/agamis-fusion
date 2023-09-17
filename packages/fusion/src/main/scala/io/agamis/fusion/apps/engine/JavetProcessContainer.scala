package io.agamis.fusion.apps.engine

import com.caoccao.javet.interop.engine.JavetEngineConfig
import org.slf4j.Logger
import akka.http.scaladsl.model.HttpRequest
import scala.concurrent.Future
import java.time.LocalDateTime
import akka.http.scaladsl.Http
import scala.util.Success
import scala.util.Failure
import akka.http.scaladsl.model.AttributeKeys
import akka.http.scaladsl.model.HttpResponse
import akka.actor.typed.ActorSystem

/** 
  * A modified JavetContainer taking care of NodeJs Application lifecycle inside Javet V8 engine
  * This modified container can run long-running processes
  *
  * @param _logger the logger the container should use
  * @param config the config to use for Javet engine
  */
class JavetProcessContainer protected[engine](implicit _logger: Logger, config: JavetEngineConfig) extends JavetContainer {
    def run(lambda: () => Unit): Unit = {
        val thread = new Thread(() => {
            try {
                lambda()
            } catch {
                case e: InterruptedException => {
                    _logger.error("<< JavetProcessContainer#run(() => Unit) > Thread for running app interupted", e)
                    throw e
                }
            }
        })
        thread.start()
    }
        
    def close(): Unit = {
        runtime.setPurgeEventLoopBeforeClose(true)
        runtime.close()
    }
}

object JavetProcessContainer {
    def forward(req: HttpRequest, port: Int, route: String, userId: String)(
        implicit
        system: ActorSystem[_],
        _logger: Logger
    ): Future[HttpResponse] = {
        // format: $address - $userId [$datetime] "$httpMethod $uri $protocol" $status $returnedBodySize
        val msgBase = String.format(
          s"%s - $userId [${LocalDateTime.now().toString}] \"%s\" %d %d",
          req.getAttribute(AttributeKeys.remoteAddress),
          s"${req.method} ${req.uri} ${req.protocol.toString}"
        )
        implicit val ec = system.executionContext
        Http()
            .singleRequest(
              req.withUri(s"http://localhost:$port/${route}")
            )
            .transformWith({
                case Success(res) => {
                    _logger.info(
                      String.format(
                        msgBase,
                        res.status.intValue,
                        res.entity.getDataBytes().map(_.length)
                      )
                    )
                    Future.successful(res)
                }
                case Failure(e) => {
                    Future.failed(e)
                }
            })
    }
}
