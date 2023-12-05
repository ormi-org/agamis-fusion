package io.agamis.fusion.api.rest.controller

import io.agamis.fusion.api.rest.model.dto.common.ApiStatus
import io.agamis.fusion.api.rest.model.dto.common.ApiStatusJsonSupport
import io.agamis.fusion.api.rest.model.dto.organization.OrganizationApiJsonSupport
import io.agamis.fusion.api.rest.model.dto.organization.OrganizationDto
import io.agamis.fusion.api.rest.model.dto.organization.OrganizationMutation
import io.agamis.fusion.core.db.datastore.cache.exceptions.NotFoundException
import io.agamis.fusion.core.shard.OrganizationShard
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.model.headers.Location
import org.apache.pekko.http.scaladsl.model.headers.RawHeader
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.Route
import org.apache.pekko.util.Timeout

import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Failure
import scala.util.Success
import scala.util.Try

class OrganizationController(shard: OrganizationShard)(implicit
    system: ActorSystem[_],
    ec: ExecutionContext
) extends ActorSystemController
    with ApiStatusJsonSupport
    with OrganizationApiJsonSupport {
    import io.agamis.fusion.core.actor.entity.Organization._

    val log = system.log

    implicit val DEFAULT_TIMEOUT = Timeout(2.seconds)

    def getSingleOrganization(id: String): Route = {
        // Check id is a valid UUID
        Try {
            UUID.fromString(id)
        } match {
            case Failure(exception) =>
                return complete(
                  StatusCodes.BadRequest,
                  ApiStatus(
                    None,
                    Some("{id} argument is not a valid UUID")
                  )
                )
            case Success(value) =>
        }
        // Get ref to an organization
        onComplete(
          // Ask Get command to get the organization
          shard.ref(id).askWithStatus(Get(_))
        ) {
            case Failure(exception) =>
                exception match {
                    case NotFoundException(_, _) =>
                        complete(StatusCodes.NotFound)
                    case other =>
                        // Handle actor conversation failure
                        complete(StatusCodes.InternalServerError)
                }

            case Success(state: Queryable) =>
                // Org is queryable
                respondWithHeader(
                  RawHeader(
                    "Content-Type",
                    "application/json; version=1"
                  )
                ) {
                    complete(OrganizationDto.from(state.org))
                }
            case Success(state: Shadow) =>
                // Org is not queryable
                complete(StatusCodes.NotFound)
        }
    }

    def createOrganization(mut: OrganizationMutation): Route = {
        onSuccess(
          shard.ref(UUID.randomUUID().toString).ask(Update(_, mut))
        ) {
            case UpdateFailure(cause) =>
                log.debug(
                  s"<< Failed to create organization with mutation ${mut.toString}"
                )
                cause match {
                    case Some(cause) => log.trace(cause.getMessage)
                }
                complete(StatusCodes.InternalServerError)
            case UpdateSuccess(created) =>
                extractRequest { req =>
                    val baseUrl =
                        req.uri.scheme + "://" + req.uri.authority.host.address + (if (
                                                                                     req.uri.authority.port != 0
                                                                                   ) ":" + req.uri.authority.port
                                                                                   else
                                                                                       "")
                    val resourceUrl =
                        s"$baseUrl/api/organizations/${created.id.toString}"
                    log.debug(
                      s"<< Successfully created organization with id ${created.id.toString} and answered with location $resourceUrl"
                    )
                    respondWithHeader(Location(resourceUrl)) {
                        complete(StatusCodes.Created)
                    }
                }
        }
    }

    def updateOrganization(id: String, mut: OrganizationMutation): Route = {
        // Check id is a valid UUID
        Try {
            UUID.fromString(id)
        } match {
            case Failure(exception) =>
                return complete(
                  StatusCodes.BadRequest,
                  ApiStatus(
                    None,
                    Some("{id} argument is not a valid UUID")
                  )
                )
            case Success(value) =>
        }
        // Get ref to an organization and check if it already exists
        val ref = shard.ref(id)
        onComplete(ref.askWithStatus(Get(_))) {
            case Failure(exception) =>
                exception match {
                    case NotFoundException(_, _) =>
                        complete(StatusCodes.NotFound)
                    case other =>
                        // Handle actor conversation failure
                        complete(StatusCodes.InternalServerError)
                }
            case Success(state: Queryable) =>
                // Handle update
                onSuccess(ref.ask(Update(_, mut))) {
                    case UpdateSuccess(updated) =>
                        log.debug(
                          s"<< Successfully updated organization with id ${updated.id.toString}"
                        )
                        complete(StatusCodes.NoContent)
                    case UpdateFailure(cause) =>
                        log.debug(
                          s"<< Failed to update organization with id $id"
                        )
                        cause match {
                            case Some(cause) => log.trace(cause.getMessage)
                        }
                        complete(
                          StatusCodes.InternalServerError,
                          ApiStatus(
                            // TODO: status code
                            None,
                            Some("Could not update organization due to")
                          )
                        )
                }
            case Success(state: Shadow) =>
                // Handle shadow case failure
                complete(
                  StatusCodes.BadRequest,
                  ApiStatus(
                    None,
                    Some(
                      "No organization found with {id} or you are trying to update a shadowed organization"
                    )
                  )
                )
        }
    }

    def deleteOrganization(id: String): Route = {
        // Check id is a valid UUID
        Try {
            UUID.fromString(id)
        } match {
            case Failure(exception) =>
                return complete(
                  StatusCodes.BadRequest,
                  ApiStatus(
                    None,
                    Some("{id} argument is not a valid UUID")
                  )
                )
            case Success(value) =>
        }
        val ref = shard.ref(id)
        onComplete(ref.askWithStatus(Get(_))) {
            case Failure(exception) =>
                exception match {
                    case NotFoundException(_, _) =>
                        complete(StatusCodes.NotFound)
                    case other =>
                        // Handle actor conversation failure
                        complete(StatusCodes.InternalServerError)
                }
            case Success(state: Queryable) =>
                // Handle deletion
                onSuccess(ref.askWithStatus(Delete(_))) {
                    case DeleteSuccess() =>
                        log.debug(
                          s"<< Successfully deleted organization with id $id"
                        )
                        complete(StatusCodes.NoContent)
                    case DeleteFailure(cause) =>
                        log.debug(
                          s"<< Failed to delete organization with id $id"
                        )
                        cause match {
                            case Some(cause) => log.trace(cause.getMessage)
                        }
                        complete(
                          StatusCodes.InternalServerError,
                          ApiStatus(
                            // TODO: status code
                            None,
                            Some("Could not delete organization due to")
                          )
                        )
                }
            case Success(state: Shadow) =>
                // Handle shadow case failure
                complete(
                  StatusCodes.BadRequest,
                  ApiStatus(
                    None,
                    Some(
                      "No organization found with {id} or you are trying to delete a shadowed organization"
                    )
                  )
                )
        }
    }
}
