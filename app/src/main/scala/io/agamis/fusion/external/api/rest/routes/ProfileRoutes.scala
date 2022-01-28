package io.agamis.fusion.external.api.rest.routes

import scala.util.Success
import scala.util.Failure

import scala.concurrent.Future
import scala.concurrent.duration._

import java.util.UUID

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{HttpEntity, ContentTypes, StatusCodes}

import akka.util.Timeout

import akka.actor.typed.{ActorSystem, ActorRef}

import io.agamis.fusion.external.api.rest.dto.{Profile, ProfileJsonProtocol}
import io.agamis.fusion.external.api.rest.actors.ProfileRepository

/**
  * Class Profile Routes
  *
  * @param buildProfileRepository
  * @param system
  */
class ProfileRoutes(buildProfileRepository: ActorRef[ProfileRepository.Command])(implicit system: ActorSystem[_]) extends ProfileJsonProtocol{

    import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
    import akka.actor.typed.scaladsl.AskPattern.Askable

    // asking someone requires a timeout and a scheduler, if the timeout hits without response
    // the ask is failed with a TimeoutException
    implicit val timeout = Timeout(3.seconds)

    lazy val routes: Route =
    concat(
        pathPrefix("profiles")(
            concat(
                // get all profiles
                get {
                    println(s"test profile route")
                    complete(StatusCodes.OK)
                },
                // create profile
                post {
                    entity(as[Profile]) { profile =>
                        onComplete(buildProfileRepository.ask(ProfileRepository.AddProfile(profile,_))) {
                            case Success(response) => response match {
                                case ProfileRepository.OK  => complete("Profile posted") 
                                case ProfileRepository.KO(cause) => cause match {
                                    case _ => complete(StatusCodes.NotImplemented -> new Error(""))
                                }
                            }
                            case Failure(reason) => complete(StatusCodes.NotImplemented -> reason)
                        }
                    }
                },
            )
        ),
        pathPrefix("profile")(
            concat(
                pathPrefix(Segment) { profileUuid: String =>
                    concat(
                        //get by id
                        get {
                            println(s"get profile uuid $profileUuid")
                            onComplete(buildProfileRepository.ask(ProfileRepository.GetProfileById(profileUuid,_))) {
                                case Success(response) => response match {
                                    case ProfileRepository.OK => complete(s"GET profile uuid : $profileUuid")
                                    case ProfileRepository.KO(cause) => cause match {
                                        case _ => complete(StatusCodes.InternalServerError -> new Error(""))
                                    }
                                }
                                case Failure(reason) => complete(StatusCodes.NotImplemented -> reason)    
                            }
                        },
                        // update profile
                        put {
                            entity(as[Profile]) { profile =>
                                println(s"received update profile for $profileUuid : $profile")
                                onComplete(buildProfileRepository.ask(ProfileRepository.UpdateProfile(profileUuid,_))) {
                                    case Success(response) => response match {
                                        case ProfileRepository.OK => complete(s"UPDATE profile uuid : $profileUuid")
                                        case ProfileRepository.KO(cause) => cause match {
                                            case _ => complete(StatusCodes.InternalServerError -> new Error(""))
                                        }
                                    }
                                    case Failure(reason) => complete(StatusCodes.NotImplemented -> reason)    
                                }
                            }
                        },
                        // delete profile
                        delete {
                            println(s"delete profile id $profileUuid")
                            onComplete(buildProfileRepository.ask(ProfileRepository.DeleteProfile(profileUuid,_))) {
                                case Success(response) => response match {
                                    case ProfileRepository.OK => complete(s"DELETE profile uuid : $profileUuid")
                                    case ProfileRepository.KO(cause) => cause match {
                                        case _ => complete(StatusCodes.InternalServerError -> new Error(""))
                                    }
                                }
                                case Failure(reason) => complete(StatusCodes.NotImplemented -> reason)    
                            }
                        }
                    )
                }      
            )
        )
    )
}