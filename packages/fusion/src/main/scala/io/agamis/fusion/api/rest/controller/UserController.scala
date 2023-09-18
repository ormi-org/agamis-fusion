package io.agamis.fusion.api.rest.controller

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.agamis.fusion.api.rest.model.dto.common.ApiStatus
import io.agamis.fusion.api.rest.model.dto.user.SingleUserResponse
import io.agamis.fusion.api.rest.model.dto.user.UserApiJsonSupport
import io.agamis.fusion.api.rest.model.dto.user.UserApiResponse
import io.agamis.fusion.api.rest.model.dto.user.UserDto
import io.agamis.fusion.api.rest.model.dto.user.UserMutation
import io.agamis.fusion.api.rest.model.dto.user.UserQuery
import io.agamis.fusion.api.rest.model.dto.user.UserQueryResponse
import io.agamis.fusion.api.rest.model.dto.user.UserErrorResponse
import io.agamis.fusion.core.actors.data.entities.UserDataBehavior
import io.agamis.fusion.core.services.UserService

import java.util.UUID
import scala.util.Failure
import scala.util.Success

class UserController(userService: UserService)(implicit system: ActorSystem[_])
    extends BehaviorBoundController[
      UserDataBehavior.Response,
      UserApiResponse,
      UserDto
    ]
    with UserApiJsonSupport {

    import io.agamis.fusion.core.actors.data.entities.UserDataBehavior.MultiUserState
    import io.agamis.fusion.core.actors.data.entities.UserDataBehavior.SingleUserState

    system.executionContext

    /** Generate a route which executes a query against User service to fetch
      * several users based on the passed filters
      *
      * @param query
      *   the query to execute
      * @return
      *   Akka Http Route
      */
    def getManyUsers(query: UserQuery): Route = {
        val serviceQuery: UserDataBehavior.Query =
            UserDataBehavior.Query(
              query.id.map(UUID.fromString(_)),
              query.username,
              query.offset,
              query.limit,
              query.createdAt,
              query.updatedAt,
              query.orderBy,
              query.include
            )
        onComplete(userService.queryUsers(serviceQuery)) {
            case Success(resp) =>
                mapToApiResponse(resp) match {
                    case SingleUserResponse(result, status) =>
                        status.code match {
                            case StatusCodes.Success(_) =>
                                complete(status.code, result)
                            case _ =>
                                complete(
                                  status.code,
                                  status.message
                                )
                        }
                    case UserQueryResponse(result, status) =>
                        status.code match {
                            case StatusCodes.Success(_) =>
                                complete(status.code, result)
                            case _ =>
                                complete(
                                  status.code,
                                  status.message
                                )
                        }
                    case _ =>
                        complete(
                          StatusCodes.InternalServerError,
                          "Bad response format from internal actor"
                        )
                }
            case Failure(cause) =>
                complete(StatusCodes.InternalServerError, cause)
        }
    }

    /** Generate a route which executes a command against User service to create
      * a single user based on the passed mutation
      *
      * @param uMut
      *   the mutation to execute
      * @return
      *   Akka Http Route
      */
    def createUser(uMut: UserMutation): Route = {
        onComplete(
          userService.createUser(
            UserDataBehavior.UserMutation(
              Some(uMut.username),
              Some(uMut.password)
            )
          )
        ) {
            case Success(resp) =>
                mapToApiResponse(resp) match {
                    case SingleUserResponse(result, _) =>
                        respondWithHeaders(
                          Location(
                            s"/api/v1/user/${result.get.id.get.toString}"
                          )
                        ) {
                            complete(StatusCodes.Created)
                        }
                    case _ =>
                        complete(
                          StatusCodes.InternalServerError,
                          "Bad response format from internal actor"
                        )
                }
            case Failure(cause) =>
                complete(StatusCodes.InternalServerError, cause)
        }
    }

    /** Generate a route which executes a query against User service to fetch a
      * single user based on the specified username
      *
      * @param username
      *   the username to query on
      * @return
      *   Akka Http Route
      */
    def getUserByUsername(username: String): Route = {
        onComplete(userService.getUserByUsername(username)) {
            case Success(resp) =>
                mapToApiResponse(resp) match {
                    case SingleUserResponse(result, status) =>
                        complete(status.code, result)
                    case _ =>
                        complete(
                          StatusCodes.InternalServerError,
                          "Bad response format from internal actor"
                        )
                }
            case Failure(cause) =>
                complete(StatusCodes.InternalServerError, cause)
        }
    }

    /** Generate a route which executes a query against User service to fetch a
      * single user based on the specified id
      *
      * @param query
      *   the query to execute
      * @param include
      *   a list of fields to include
      * @return
      *   Akka Http Route
      */
    def getSingleUser(id: String, include: List[String]): Route = {
        onComplete(
          userService.getUserById(
            UUID.fromString(id),
            include
          )
        ) {
            case Success(resp) =>
                mapToApiResponse(resp) match {
                    case SingleUserResponse(
                          result,
                          status
                        ) =>
                        complete(status.code, result)
                    case _ =>
                        complete(
                          StatusCodes.InternalServerError,
                          "Bad response format from internal actor"
                        )
                }
            case Failure(cause) =>
                complete(
                  StatusCodes.InternalServerError,
                  cause
                )
        }
    }

    /** Generate a route which executes a command against User service to update
      * a single user based on the passed mutation
      *
      * @param id
      *   the id of the user to update
      * @param uMut
      *   the mutation to execute
      * @return
      *   Akka Http Route
      */
    def updateUser(id: String, uMut: UserMutation): Route = {
        onComplete(
          userService.updateUser(
            UUID.fromString(id),
            UserDataBehavior.UserMutation(
              Some(uMut.username),
              Some(uMut.password)
            )
          )
        ) {
            case Success(resp) =>
                mapToApiResponse(resp) match {
                    case SingleUserResponse(
                          result,
                          status
                        ) =>
                        complete(status.code, result)
                    case _ =>
                        complete(
                          StatusCodes.InternalServerError,
                          "Bad response format from internal actor"
                        )
                }
            case Failure(cause) =>
                complete(
                  StatusCodes.InternalServerError,
                  cause
                )
        }
    }

    /** Generate a route which executes a command against User service to update
      * a single user based on the passed mutation
      *
      * @param id
      *   the id of the user to update
      * @param uMut
      *   the mutation to execute
      * @return
      *   Akka Http Route
      */
    def deleteUser(id: String): Route = {
        onComplete(
          userService.deleteUser(UUID.fromString(id))
        ) {
            case Success(resp) =>
                mapToApiResponse(resp) match {
                    case SingleUserResponse(result, status) =>
                        complete(status.code, result)
                    case _ =>
                        complete(
                          StatusCodes.InternalServerError,
                          "Bad response format from internal actor"
                        )
                }
            case Failure(cause) =>
                complete(StatusCodes.InternalServerError, cause)
        }
    }

    /** Map service response to Api Response
      *
      * @param r
      *   initial service response
      */
    protected def mapToApiResponse(
        r: UserDataBehavior.Response
    ): UserApiResponse = {
        (r match {
            case SingleUserState(_, result, status) =>
                status match {
                    case ok: UserDataBehavior.Ok =>
                        result match {
                            case Some(u) =>
                                SingleUserResponse(
                                  Some(excludeFields(u)),
                                  ApiStatus(StatusCodes.OK, None)
                                )
                            case None =>
                                SingleUserResponse(
                                  None,
                                  ApiStatus(
                                    StatusCodes.InternalServerError,
                                    Some(
                                      s"Service responded '${ok.msg}' but provided an empty result"
                                    )
                                  )
                                )
                        }
                    case nfound: UserDataBehavior.NotFound =>
                        SingleUserResponse(
                          None,
                          ApiStatus(StatusCodes.NotFound, Some(nfound.msg))
                        )
                    case exception: UserDataBehavior.InternalException =>
                        SingleUserResponse(
                          None,
                          ApiStatus(
                            StatusCodes.InternalServerError,
                            Some(exception.msg)
                          )
                        )
                }
            case MultiUserState(_, result, status) =>
                status match {
                    case _: UserDataBehavior.Ok =>
                        UserQueryResponse(
                          result.map(excludeFields(_)),
                          ApiStatus(StatusCodes.OK, None)
                        )
                    case nfound: UserDataBehavior.NotFound =>
                        UserQueryResponse(
                          List(),
                          ApiStatus(StatusCodes.NotFound, Some(nfound.msg))
                        )
                    case exception: UserDataBehavior.InternalException =>
                        SingleUserResponse(
                          None,
                          ApiStatus(
                            StatusCodes.InternalServerError,
                            Some(exception.msg)
                          )
                        )
                }
            // case _ => UserErrorResponse(ApiStatus(StatusCodes.NotFound, None))
        })
    }

    /** Default exclude relations deep unpopulated fields
      * @param user
      *   the user dto to filter
      */
    protected def excludeFields(user: UserDto): UserDto = {
        return user.copy(
          profiles = user.profiles match {
              case Some(profiles) =>
                  Some(profiles.map {
                      _.copy(
                        emails = None,
                        organization = None,
                        permissions = None
                      )
                  })
              case None => None
          }
        )
    }
}
