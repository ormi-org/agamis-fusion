package io.agamis.fusion.external.api.rest.routes

import scala.util.Success
import scala.util.Failure

import scala.concurrent.duration._

import java.util.UUID

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{HttpEntity, ContentTypes, StatusCodes}

import akka.util.Timeout

import akka.actor.typed.{ActorSystem, ActorRef}
import scala.concurrent.Future

import io.agamis.fusion.external.api.rest.dto.user.{UserDto, UserJsonSupport}
import io.agamis.fusion.external.api.rest.actors.UserRepository
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.headers.RawHeader
import io.agamis.fusion.external.api.rest.actors.UserRepository.{
  OK,
  KO,
  CreateUserResponse
}
import akka.http.scaladsl.model.ResponseEntity
import akka.http.scaladsl.model.StatusCode

/** Class User Routes
  *
  * @param userRepository
  * @param system
  */
class UserRoutes(userRepository: ActorRef[UserRepository.Command])(implicit
    system: ActorSystem[_]
) extends UserJsonSupport {

  import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
  import akka.actor.typed.scaladsl.AskPattern.Askable

  import io.agamis.fusion.external.api.rest.authorization.JwtAuthorization._

  // asking someone requires a timeout and a scheduler, if the timeout hits without response
  // the ask is failed with a TimeoutException
  implicit val timeout = Timeout(3.seconds)

  lazy val routes: Route =
    concat(
      pathPrefix("users")(
        concat(
          // get all users
          get {
              println(s"test group route")
              complete(StatusCodes.OK)
          },
          // create user
          post {
            entity(as[UserDto]) { user =>
              onComplete(
                userRepository.ask(UserRepository.CreateUser(user, _))
              ) {
                case Success(response) =>
                  response match {
                    case CreateUserResponse(user) => {
                      complete(
                        StatusCodes.OK,
                        user
                      )
                    }
                  }
                case Failure(reason) =>
                  complete(
                    HttpResponse(
                      StatusCodes.InternalServerError,
                      entity = reason.toString()
                    )
                  )
              }
            }
          }
        )
      ),
      pathPrefix("user")(
        concat(
          //get by id
          get {
            parameter("id".as[String]) { (userUuid: String) =>
              optionalHeaderValueByName("Authorization") {
                case Some(authHeaderContent) =>
                  if (
                    !authHeaderContent.matches(
                      "^Bearer\\s[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$"
                    )
                  ) complete(HttpResponse(StatusCodes.Unauthorized))
                  else {
                    val token: String =
                      authHeaderContent.replaceAll("^Bearer\\s$", "")
                    if (isTokenValid(token)) {
                      if (isTokenExpired(token)) {
                        complete(
                          HttpResponse(
                            StatusCodes.Unauthorized,
                            entity = "Token expired"
                          )
                        )
                      } else {
                        onComplete(
                          userRepository.ask(
                            UserRepository.GetUserById(userUuid, _)
                          )
                        ) {
                          case Success(user) => complete(StatusCodes.OK)
                          case Failure(reason) =>
                            reason match {
                              case _ =>
                                complete(
                                  HttpResponse(
                                    StatusCodes.InternalServerError,
                                    entity = reason.toString()
                                  )
                                )
                            }
                        }
                      }
                    } else {
                      complete(
                        HttpResponse(
                          StatusCodes.Unauthorized,
                          entity = "Token is invalid"
                        )
                      )
                    }
                  }
                case _ =>
                  complete(
                    HttpResponse(
                      StatusCodes.Unauthorized,
                      entity = "No token provided"
                    )
                  )
              }
            }
          },
          // update user
          put {
            parameter("id".as[String]) { (userUuid: String) =>
              entity(as[UserDto]) { user =>
                println(s"received update user for $userUuid : $user")
                onComplete(
                  userRepository.ask(UserRepository.UpdateUser(user, _))
                ) {
                  case Success(response) =>
                    response match {
                      case UserRepository.OK() => complete("User updated")
                      case UserRepository.KO(cause) =>
                        cause match {
                          case _ =>
                            complete(
                              StatusCodes.NotImplemented -> new Error("")
                            )
                        }
                    }
                  case Failure(reason) =>
                    complete(StatusCodes.NotImplemented -> reason)
                }
              }
            }
          },
          // delete user
          delete {
            parameter("id".as[String]) { (userUuid: String) =>
              println(s"delete user id $userUuid")
              entity(as[UserDto]) { user =>
                onComplete(
                  userRepository.ask(UserRepository.DeleteUser(user, _))
                ) {
                  case Success(response) =>
                    response match {
                      case UserRepository.OK() => complete("User deleted")
                      case UserRepository.KO(cause) =>
                        cause match {
                          case _ =>
                            complete(
                              StatusCodes.NotImplemented -> new Error("")
                            )
                        }
                    }
                  case Failure(reason) =>
                    complete(StatusCodes.NotImplemented -> reason)
                }
              }
            }
          }
        )
      )
    )
}
