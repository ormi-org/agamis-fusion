package io.agamis.fusion.api.rest.routes.apps

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
import io.agamis.fusion.api.rest.common.marshalling.StringArrayUnmarshaller
import io.agamis.fusion.api.rest.model.dto.organization.OrganizationDto
import io.agamis.fusion.api.rest.model.dto.profile.ProfileDto
import io.agamis.fusion.api.rest.model.dto.user.UserApiJsonSupport
import io.agamis.fusion.api.rest.model.dto.user.UserApiResponse
import io.agamis.fusion.api.rest.model.dto.user.UserDto
import io.agamis.fusion.api.rest.model.dto.user.UserMutation
import io.agamis.fusion.api.rest.model.dto.user.UserQuery
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
import akka.http.scaladsl.model.HttpHeader

/** Class User Routes
  *
  * @param system
  * @param data
  */
class AppRoutes()(implicit system: ActorSystem[_]) {

    import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
    import akka.actor.typed.scaladsl.AskPattern.Askable

    import io.agamis.fusion.api.rest.model.dto.user.SingleUserResponse
    import io.agamis.fusion.api.rest.model.dto.user.UserQueryResponse
    import io.agamis.fusion.api.rest.model.dto.common.typed.ApiStatus

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
                            implicit val logger = system.log
                            // TODO: implement proxying through container registry
                            failWith(new NotImplementedError)
                        }
                    }
                  )
              }
            )
          ),
          path(Segment) { appId: String =>
            concat(
                pathSuffix("bootstrap") (
                    failWith(new NotImplementedError)
                    // Redirect to client
                ),
                path("client") (
                    concat(
                        get {
                            failWith(new NotImplementedError)
                        },
                        pathSuffix("assets/endpoints.json") (
                            headerValueByName("agamis-io-fusion-app-sessionId") { sessionId: String =>
                                // TODO : implement endpoints.json generation
                                failWith(new NotImplementedError)
                            }
                        ),
                    )
                )
            )
          }
        )
}
