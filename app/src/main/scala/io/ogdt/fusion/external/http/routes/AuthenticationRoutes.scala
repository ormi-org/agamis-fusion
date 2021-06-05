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
import io.ogdt.fusion.external.http.actors.AuthenticationRepository
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.headers.RawHeader
import io.ogdt.fusion.external.http.actors.UserRepository

/**
  * Class User Routes
  *
  * @param buildUserRepository
  * @param system
  */
class AuthenticationRoutes(buildAuthenticationRepository: ActorRef[AuthenticationRepository.Command])(implicit system: ActorSystem[_]) extends UserJsonProtocol {

    import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
    import akka.actor.typed.scaladsl.AskPattern.Askable
    
    import io.ogdt.fusion.external.http.authorization.JwtAuthorization._

    import io.ogdt.fusion.core.data.security.utils.HashPassword._

    // asking someone requires a timeout and a scheduler, if the timeout hits without response
    // the ask is failed with a TimeoutException
    implicit val timeout = Timeout(3.seconds)

   lazy val routes: Route =
    concat(
        pathPrefix("authenticate")( 
            concat( 
                get {
                    parameter("token".as[String],"refreshToken".as[String]) { (token: String, refreshToken:String) => 
                        if(isTokenValid(token)) {
                            if(isTokenValid(refreshToken)) {
                                if(!isTokenExpired(token)) {
                                    onComplete(buildAuthenticationRepository.ask(AuthenticationRepository.AuthenticationWithToken(token,_))) {
                                        case Success(response) => response match {
                                            case AuthenticationRepository.OK => complete("OK")
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
                                redirect("/login", StatusCodes.PermanentRedirect)
                            }
                        } else {
                            complete(StatusCodes.Forbidden -> new Error("Username and password mismatch"))
                        }
                    }
                }
            )
        ), 
        pathPrefix("login")(
            get {
                parameter("username".as[String],"password".as[String]) { (username:String, password: String) => 
                    if(checkPassword(username, password)) {
                        onComplete(buildAuthenticationRepository.ask(AuthenticationRepository.Login(username,password,_))) {
                            case Success(response) => response match {
                                case AuthenticationRepository.OK => complete("OK")
                                case AuthenticationRepository.KO(cause) => cause match {
                                    case _ => complete(StatusCodes.Forbidden -> new Error("Forbiden access"))
                                }
                            }
                            case Failure(reason) => complete(StatusCodes.NotImplemented -> reason)
                        }
                    } else {
                        complete(StatusCodes.Forbidden -> new Error("Username and password mismatch"))
                    }
                }
            }
        )
    )
}