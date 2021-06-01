package io.ogdt.fusion.external.http.routes

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

import io.ogdt.fusion.external.http.entities.{User, UserJsonProtocol}
import io.ogdt.fusion.external.http.actors.UserRepository
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.headers.RawHeader

/**
  * Class User Routes
  *
  * @param buildUserRepository
  * @param system
  */
class UserRoutes(buildUserRepository: ActorRef[UserRepository.Command])(implicit system: ActorSystem[_]) extends UserJsonProtocol {

    import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
    import akka.actor.typed.scaladsl.AskPattern.Askable
    
    import io.ogdt.fusion.external.http.authorization.JwtAuthorization._

    // asking someone requires a timeout and a scheduler, if the timeout hits without response
    // the ask is failed with a TimeoutException
    implicit val timeout = Timeout(3.seconds)

   lazy val routes: Route =
    concat(
        pathPrefix("users")(
            concat(
                // get all users
                // get {
                //     println(s"test group route")
                //     complete(StatusCodes.OK)
                // },
                // create user
                post {
                    entity(as[User]) { user =>
                        //val token = createToken(user.username,1)
                        val token: String = createToken()
                        // if checkPassword(user.username, user.password)
                        onComplete(buildUserRepository.ask(UserRepository.AddUser(user,token,_))) {
                            case Success(value) => respondWithHeader(RawHeader("Access-Token",token)) { complete(StatusCodes.OK) }  
                            case Failure(reason) => complete(HttpResponse(StatusCodes.InternalServerError, entity = reason.toString()))
                        }
                    }
                }
            )
        ),
        pathPrefix("user")(
            concat(
                // get refresh token
                //get by name
                // get {
                //     parameter("name".as[String]) { (name: String) => 
                //         optionalHeaderValueByName("Authorization") {
                //             case Some(token) =>
                //                 if(tokenValidation(token)) {
                //                     if (decode(token)) {
                //                         onComplete(buildUserRepository.ask(UserRepository.GetUserByName(name,token,_))) {
                //                             case Success(user) => complete(StatusCodes.OK -> user)
                //                             case Failure(reason) => reason match {
                //                                 case _ => complete(HttpResponse(StatusCodes.InternalServerError, entity = reason.toString()))
                //                             }
                //                         } 
                //                     } else { complete("User authorrized") }
                //                 } else { complete(HttpResponse(StatusCodes.Unauthorized, entity = "Token is invalid" )) }
                //             case _ => complete(HttpResponse(StatusCodes.Unauthorized, entity = "No token provided"))
                //         }
                //     }
                // },
                //get by id
                get {
                    parameter("id".as[String]) { (userUuid: String) => 
                        optionalHeaderValueByName("Authorization") {
                            case Some(authHeaderContent) =>
                                if (!authHeaderContent.matches("^Bearer\\s[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$")) complete(HttpResponse(StatusCodes.Unauthorized))
                                else {
                                    val token: String = authHeaderContent.replaceAll("^Bearer\\s$", "")
                                    if(isTokenValid(token)) {
                                        if (isTokenExpired(token)) {
                                            complete(HttpResponse(StatusCodes.Unauthorized, entity = "Token expired"))
                                        } else {
                                            onComplete(buildUserRepository.ask(UserRepository.GetUserById(userUuid,token,_))) {
                                                case Success(user) => complete(StatusCodes.OK)
                                                case Failure(reason) => reason match {
                                                    case _ => complete(HttpResponse(StatusCodes.InternalServerError, entity = reason.toString()))
                                                } 
                                            }
                                        }
                                    } else { complete(HttpResponse(StatusCodes.Unauthorized, entity = "Token is invalid" )) }
                                }
                            case _ => complete(HttpResponse(StatusCodes.Unauthorized, entity = "No token provided"))
                        }
                    }
                },
                // update user
                put {
                    parameter("id".as[String]) { (userUuid: String) => 
                        entity(as[User]) { user =>
                            println(s"received update user for $userUuid : $user")
                            onComplete(buildUserRepository.ask(UserRepository.UpdateUser(user,_))) {
                                case Success(response) => response match {
                                    case UserRepository.OK  => complete("User updated") 
                                    case UserRepository.KO(cause) => cause match {
                                        case _ => complete(StatusCodes.NotImplemented -> new Error(""))
                                    }
                                }
                                case Failure(reason) => complete(StatusCodes.NotImplemented -> reason)
                            }
                        }
                    }
                },
                // delete user
                delete {
                        parameter("id".as[String]) { (userUuid: String) => 
                            println(s"delete user id $userUuid")
                            entity(as[User]) { user =>
                            onComplete(buildUserRepository.ask(UserRepository.DeleteUser(user,_))) {
                                case Success(response) => response match {
                                    case UserRepository.OK  => complete("User deleted") 
                                    case UserRepository.KO(cause) => cause match {
                                        case _ => complete(StatusCodes.NotImplemented -> new Error(""))
                                    }
                                }
                                case Failure(reason) => complete(StatusCodes.NotImplemented -> reason)
                            }
                        }
                    }
                }
            )
        ),
        pathPrefix("authenticate")( 
            concat( 
                get {
                    parameter("token".as[String],"username".as[String],"password".as[String]) { (token: String, username:String, password: String) => 
                        if(checkPassword(username, password)) {
                            if(!isTokenExpired(token)) {
                                onComplete(buildUserRepository.ask(UserRepository.Authenfication(token,username,password,_))) {
                                    case Success(response) => response match {
                                        case UserRepository.OK => complete("OK")
                                        case UserRepository.KO(cause) => cause match {
                                            case _ => complete(StatusCodes.Forbidden -> new Error("Forbiden access"))
                                        }
                                    }
                                    case Failure(reason) => complete(StatusCodes.NotImplemented -> reason)
                                }
                            } else {
                                complete(StatusCodes.Forbidden -> new Error("Token has expired"))    
                            }
                        } else {
                            complete(StatusCodes.Forbidden -> new Error("Username and password mismatch"))
                        }
                    }
                }
            )
        )
    )
}