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

import io.agamis.fusion.external.api.rest.dto.user.UserDto
import io.agamis.fusion.external.api.rest.actors.AuthenticationRepository
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.headers.RawHeader
import io.agamis.fusion.external.api.rest.actors.UserRepository

/**
  * Class User Routes
  *
  * @param buildUserRepository
  * @param system
  */
class AuthenticationRoutes(buildAuthenticationRepository: ActorRef[AuthenticationRepository.Command])(implicit system: ActorSystem[_]) {

    import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
    import akka.actor.typed.scaladsl.AskPattern.Askable
    
    import io.agamis.fusion.external.api.rest.common.Jwt._

    import io.agamis.fusion.core.data.security.utils.HashPassword._

    // asking someone requires a timeout and a scheduler, if the timeout hits without response
    // the ask is failed with a TimeoutException
    implicit val timeout = Timeout(3.seconds)

    lazy val routes: Route =
    concat(
        pathPrefix("auth")( 
            concat( 
                get {
                    parameter("token".as[String],"refreshToken".as[String]) { (token: String, refreshToken: String) => 
                        if((isTokenValid(token)) || (isTokenValid(refreshToken))) {
                                if((!isTokenExpired(token)) || (!isTokenExpired(refreshToken))) {
                                    onComplete(buildAuthenticationRepository.ask(AuthenticationRepository.AuthenticationWithToken(token,_))) {
                                        case Success(response) => response match {
                                            case AuthenticationRepository.OK => complete("Connected")
                                            case AuthenticationRepository.KO(cause) => cause match {
                                                case _ => complete(StatusCodes.Forbidden -> new Error("Forbiden access"))
                                            }
                                        }
                                        case Failure(reason) => complete(StatusCodes.NotImplemented -> reason)
                                    }
                                } else {
                                    complete(StatusCodes.Forbidden -> new Error("Token has expired"))    
                                }
                        } else {
                            complete(StatusCodes.Forbidden -> new Error("Token not valid"))
                        }
                    }
                }
            )
        ), 
        pathPrefix("login")(
            get {
                parameter("username".as[String],"password".as[String]) { (username: String, password: String) => 
                    if(checkPassword(password)) { 
                        onComplete(buildAuthenticationRepository.ask(AuthenticationRepository.Login(username,password,_))) {
                            case Success(response) => response match {
                                case AuthenticationRepository.OK => respondWithHeaders(RawHeader("Access-Token", createToken()), RawHeader("Refresh-Token", refreshToken())) {
                                    complete(HttpResponse(StatusCodes.OK, entity = "OK"))
                                }
                                case AuthenticationRepository.KO(cause) => cause match {
                                    case _ => complete(StatusCodes.Forbidden -> new Error("Forbiden access"))
                                }
                            }
                            case Failure(reason) => complete(StatusCodes.NotImplemented -> reason)
                        }
                    } else {
                        complete(StatusCodes.Forbidden -> new Error("Password is wrong"))
                    }
                }
            }
        ), 
        pathPrefix("logout")(
            get {
                parameter("token".as[String],"refreshToken".as[String]) { (token: String, refreshToken:String) => 
                    onComplete(buildAuthenticationRepository.ask(AuthenticationRepository.Logout(token,refreshToken,_))) {
                        case Success(response) => response match {
                            case AuthenticationRepository.OK => complete("Token and refresh token has been deleted")
                            case AuthenticationRepository.KO(cause) => cause match {
                                case _ => complete(StatusCodes.Forbidden -> new Error("Problems")) // TODO : à changer
                            }
                        }
                        case Failure(reason) => complete(StatusCodes.BadGateway -> reason)
                    }
                }
            }
        )
    )
}