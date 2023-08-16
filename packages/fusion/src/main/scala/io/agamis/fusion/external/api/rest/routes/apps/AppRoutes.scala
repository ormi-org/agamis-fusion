package io.agamis.fusion.external.api.rest.routes.apps

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.sharding.typed.scaladsl.EntityRef
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import io.agamis.fusion.core.actors.data.DataActor
import io.agamis.fusion.core.actors.data.entities.UserDataBehavior
import io.agamis.fusion.core.services.UserService
import io.agamis.fusion.external.api.rest.common.marshalling.StringArrayUnmarshaller
import io.agamis.fusion.external.api.rest.dto.organization.OrganizationDto
import io.agamis.fusion.external.api.rest.dto.profile.ProfileDto
import io.agamis.fusion.external.api.rest.dto.user.UserApiJsonSupport
import io.agamis.fusion.external.api.rest.dto.user.UserApiResponse
import io.agamis.fusion.external.api.rest.dto.user.UserDto
import io.agamis.fusion.external.api.rest.dto.user.UserMutation
import io.agamis.fusion.external.api.rest.dto.user.UserQuery
import spray.json._

import java.time.Instant
import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Failure
import scala.util.Success
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import java.time.LocalDateTime
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.AttributeKeys
import org.slf4j.Logger

/** Class User Routes
  *
  * @param system
  * @param data
  */
class AppRoutes()(implicit system: ActorSystem[_]) {

    import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
    import akka.actor.typed.scaladsl.AskPattern.Askable

    import io.agamis.fusion.external.api.rest.dto.user.SingleUserResponse
    import io.agamis.fusion.external.api.rest.dto.user.UserQueryResponse
    import io.agamis.fusion.external.api.rest.dto.common.typed.ApiStatus

    import io.agamis.fusion.core.actors.data.entities.UserDataBehavior.MultiUserState
    import io.agamis.fusion.core.actors.data.entities.UserDataBehavior.SingleUserState

    private implicit val ec: ExecutionContext = system.executionContext

    import io.agamis.fusion.core.actors.data.entities.UserDataBehavior.Field

    val routes =
        concat(
          path("routing")(
            concat(
              path(Segment) { sessionId: String =>
                  concat(
                    path(Remaining) { route: String =>
                        extractRequest { req =>
                            // Retrieve endpoint details (port)
                            val port   = 10000
                            val userId = "90e5807a-a45e-4036-a7cf-958037909f43"
                            // Forward
                            implicit val logger = system.log
                            onComplete(
                              AppRoutes.forward(req, port, route, userId)
                            ) {
                                case Success(resp) =>
                                    complete(resp)
                                case Failure(cause) =>
                                    complete(
                                      StatusCodes.InternalServerError,
                                      cause
                                    )
                            }
                        }
                    }
                  )
              }
            )
          )
        )
}

object AppRoutes {
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
