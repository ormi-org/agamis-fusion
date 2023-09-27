package io.agamis.fusion.api.rest.routes

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import io.agamis.fusion.api.rest.model.dto.organization.OrganizationDto
import io.agamis.fusion.api.rest.model.dto.organization.OrganizationJsonSupport
import io.agamis.fusion.api.rest.model.dto.organization.OrganizationMutation
import io.agamis.fusion.api.rest.model.dto.organization.OrganizationMutationJsonSupport
import io.agamis.fusion.core.shard.OrganizationShard

import java.util.UUID
import scala.concurrent.duration._

/** Class Organization Routes
  *
  * @param buildOrganizationRepository
  * @param system
  */
class OrganizationRoutes(implicit system: ActorSystem[_])
    extends OrganizationJsonSupport
    with OrganizationMutationJsonSupport {

    import io.agamis.fusion.core.actor.entity.Organization._

    // asking someone requires a timeout and a scheduler, if the timeout hits without response
    // the ask is failed with a TimeoutException
    implicit val timeout = Timeout(3.seconds)

    lazy val routes: Route =
        concat(
          pathPrefix("organizations")(
            concat(
              // get all organizations
              get {
                  complete(StatusCodes.NotImplemented)
              },
              // create organization
              post {
                  entity(as[OrganizationMutation]) { mut =>
                      onSuccess(
                        OrganizationShard
                            .ref(UUID.randomUUID.toString)
                            .ask(Update(_, mut))
                      ) {
                          case UpdateFailure() =>
                              complete(StatusCodes.InternalServerError)
                          case UpdateSuccess(o) =>
                              extractRequest { request =>
                                  val baseUrl =
                                      request.uri.scheme + "://" + request.uri.authority.host.address
                                  respondWithHeader(
                                    Location(
                                      s"${baseUrl}/api/organization/${o.id.toString}"
                                    )
                                  ) {
                                      complete(StatusCodes.Created)
                                  }
                              }
                      }
                  }
              }
            )
          ),
          pathPrefix("organization")(
            concat(
              // get by id
              get {
                  path(Segment) { id: String =>
                      onSuccess(
                        OrganizationShard.ref(id).ask(Get(_))
                      ) {
                          case Queryable(org) =>
                              complete(
                                StatusCodes.OK,
                                OrganizationDto.from(org)
                              )
                          case Shadow() =>
                              complete(StatusCodes.Forbidden)
                      }
                  }
              },
              // update organization
              put {
                  path(Segment) { id: String =>
                      entity(as[OrganizationMutation]) { mut =>
                          onSuccess(
                            OrganizationShard.ref(id).ask(Update(_, mut))
                          ) {
                              case UpdateSuccess(o) =>
                                  complete(StatusCodes.NoContent)
                              case UpdateFailure() =>
                                  complete(StatusCodes.InternalServerError)
                          }
                      }
                  }
              },
              // delete organization
              delete {
                  path(Segment) { id: String =>
                      onSuccess(
                        OrganizationShard.ref(id).ask(Delete(_))
                      ) {
                          case DeleteSuccess() =>
                              complete(StatusCodes.NoContent)
                          case DeleteFailure() =>
                              complete(StatusCodes.InternalServerError)
                      }
                  }
              }
            )
          )
        )
}
