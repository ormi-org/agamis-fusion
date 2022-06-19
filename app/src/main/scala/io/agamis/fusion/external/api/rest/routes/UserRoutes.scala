package io.agamis.fusion.external.api.rest.routes

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import io.agamis.fusion.core.actors.data.DataActor
import io.agamis.fusion.core.actors.data.entities.UserDataBehavior
import io.agamis.fusion.external.api.rest.dto.user.UserApiJsonSupport
import io.agamis.fusion.external.api.rest.dto.user.UserApiResponse
import io.agamis.fusion.external.api.rest.dto.user.UserDto
import io.agamis.fusion.external.api.rest.dto.user.UserQuery

import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Failure
import scala.util.Success
import io.agamis.fusion.external.api.rest.dto.user.UserMutation

/** Class User Routes
  *
  * @param system
  * @param data
  */
class UserRoutes(data: ActorRef[ShardingEnvelope[DataActor.Command]])(implicit system: ActorSystem[_]) extends UserApiJsonSupport {

  import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
  import akka.actor.typed.scaladsl.AskPattern.Askable

  import io.agamis.fusion.external.api.rest.dto.user.SingleUserResponse
  import io.agamis.fusion.external.api.rest.dto.user.UserQueryResponse
  import io.agamis.fusion.external.api.rest.dto.common.typed.ApiStatus

  import io.agamis.fusion.core.actors.data.entities.UserDataBehavior.MultiUserState
  import io.agamis.fusion.core.actors.data.entities.UserDataBehavior.SingleUserState

  private implicit val ec: ExecutionContext = system.executionContext

  // asking someone requires a timeout and a scheduler, if the timeout hits without response
  // the ask is failed with a TimeoutException
  implicit val timeout = Timeout(3.seconds)

  def handleDataResponse(f: Future[UserDataBehavior.Response]): Future[UserApiResponse] = {
    f.map {
      case SingleUserState(entityId@_, result, status) =>
        status match {
          case ok: UserDataBehavior.Ok =>
            result match {
              case Some(u) => SingleUserResponse(Some(UserDto.from(u)), ApiStatus(StatusCodes.OK, ok.msg))
              case None => SingleUserResponse(None, ApiStatus(StatusCodes.InternalServerError, s"Datastore responded '${ok.msg}' but provided an empty result"))
            }
          case nfound: UserDataBehavior.NotFound =>
            SingleUserResponse(None, ApiStatus(StatusCodes.NotFound, nfound.msg))
          case exception: UserDataBehavior.InternalException => 
            SingleUserResponse(None, ApiStatus(StatusCodes.InternalServerError, exception.msg))
        }
      case MultiUserState(entityId@_, result, status) => 
        status match {
          case ok: UserDataBehavior.Ok =>
            UserQueryResponse(result.map(u => UserDto.from(u)), ApiStatus(StatusCodes.OK, ok.msg))
          case nfound: UserDataBehavior.NotFound =>
            UserQueryResponse(List(), ApiStatus(StatusCodes.NotFound, nfound.msg))
          case exception: UserDataBehavior.InternalException => 
            SingleUserResponse(None, ApiStatus(StatusCodes.InternalServerError, exception.msg))
        }
    }
  }

  def getUserById(id: UUID): Future[UserApiResponse] = {
    handleDataResponse(data.ask { ref: ActorRef[UserDataBehavior.Response] =>
      ShardingEnvelope("user-%s".format(id.toString()), UserDataBehavior.GetUserById(ref, id))
    })
  }

  def getUserByUsername(username: String): Future[UserApiResponse] = {
    handleDataResponse(data.ask { ref: ActorRef[UserDataBehavior.Response] =>
      ShardingEnvelope("user-uname-%s".format(username), UserDataBehavior.GetUserByUsername(ref, username))
    })
  }

  def queryUsers(query: UserDataBehavior.Query): Future[UserApiResponse] = {
    handleDataResponse(data.ask { ref: ActorRef[UserDataBehavior.Response] =>
      ShardingEnvelope("user-query-%d".format(query.hashCode()), UserDataBehavior.ExecuteQuery(ref, query))
    })
  }

  def createUser(uMut: UserDataBehavior.UserMutation): Future[UserApiResponse] = {
    handleDataResponse(data.ask { ref: ActorRef[UserDataBehavior.Response] =>
      ShardingEnvelope("user-crt-%s".format(UUID.randomUUID()), UserDataBehavior.CreateUser(ref, uMut))
    })
  }

  def updateUser(id: UUID, uMut: UserDataBehavior.UserMutation): Future[UserApiResponse] = {
    handleDataResponse(data.ask { ref: ActorRef[UserDataBehavior.Response] =>
      ShardingEnvelope("user-%s".format(id.toString()), UserDataBehavior.UpdateUser(ref, uMut))
    })
  }

  lazy val routes: Route =
    concat(
      pathPrefix("users")(
        concat(
          // query on all users
          get {
            parameters(
              "id".as[List[String]],
              "username".as[List[String]],
              "offset".as[Long],
              "limit".as[Long],
              "created_at".as[List[(String, String)]],
              "updated_at".as[List[(String, String)]]
            ).as(UserQuery.apply _) { queryString =>
              val query: UserDataBehavior.Query = UserDataBehavior.Query(
                queryString.id.map(UUID.fromString(_)),
                queryString.username,
                queryString.offset,
                queryString.limit,
                queryString.createdAt,
                queryString.updatedAt
              )
              onComplete(queryUsers(query)) {
                case Success(resp: UserApiResponse) =>
                  resp match {
                    case UserQueryResponse(result, status) => complete(status.code, result)
                    case _ => complete(StatusCodes.InternalServerError, "Bad response format from internal actor")
                  }
                case Failure(cause) => complete(StatusCodes.InternalServerError, cause)
              }
            }
            complete(StatusCodes.NotImplemented)
          },
          // create user
          post {
            entity(as[UserMutation]) { uMut =>
              onComplete(createUser(UserDataBehavior.UserMutation(uMut.username, uMut.password))) {
                case Success(resp: UserApiResponse) =>
                  resp match {
                    case SingleUserResponse(result, status) => complete(status.code, result)
                    case _ => complete(StatusCodes.InternalServerError, "Bad response format from internal actor")
                  }
                case Failure(cause) => complete(StatusCodes.InternalServerError, cause)
              }
            }
          },
        )
      ),
      pathPrefix("user")(
        concat(
          //get by id
          get {
            path(Segment) { id: String =>
              onComplete(getUserById(UUID.fromString(id))) {
                case Success(resp: UserApiResponse) => 
                  resp match {
                    case SingleUserResponse(result, status) => complete(status.code, result)
                    case _ => complete(StatusCodes.InternalServerError, "Bad response format from internal actor")
                  }
                case Failure(cause) => complete(StatusCodes.InternalServerError, cause)
              }
            };
            parameters("username".as[String]) { (username) =>
              onComplete(getUserByUsername(username)) {
                case Success(resp: SingleUserResponse) =>
                  resp match {
                    case SingleUserResponse(result, status) => complete(status.code, result)
                    case _ => complete(StatusCodes.InternalServerError, "Bad response format from internal actor")
                  }
                case Failure(cause) => complete(StatusCodes.InternalServerError, cause)
              }
            }
          },
          // update user
          // put {
          //   path(Segment) { id: String =>
          //     entity(as[UserDto]) { user =>
          //       onComplete(updateUser(id, 
          //         UserDataBehavior.UserMutation(
          //           user.
          //         )
          //       ))
          //       complete(StatusCodes.NotImplemented)
          //     }
          //   }
          // },
          // delete user
          delete {
            path(Segment) { id: String =>
              complete(StatusCodes.NotImplemented)
            }
          }
        )
      )
    )
}
