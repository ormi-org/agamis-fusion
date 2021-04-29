package io.ogdt.fusion.external.http.routes

import scala.concurrent.ExecutionContext
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{HttpEntity, ContentTypes, StatusCodes}

import java.util.UUID

import akka.util.Timeout

import scala.concurrent.duration._

import akka.actor.typed.{ActorSystem, ActorRef}
import scala.concurrent.Future

import io.ogdt.fusion.external.http.entities.Profile
import io.ogdt.fusion.external.http.actors.ProfileRepository
import io.ogdt.fusion.external.http.entities.ProfileJsonProtocol

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
                        val operationPerformed: Future[ProfileRepository.Response] = 
                            buildProfileRepository.ask(ProfileRepository.AddProfile(profile,_))
                        onSuccess(operationPerformed) {                    
                            case ProfileRepository.OK  => complete("Profile added")
                            case ProfileRepository.KO(reason) => complete(StatusCodes.InternalServerError -> reason)
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
                            complete(StatusCodes.OK)
                        },
                        // update profile
                        put {
                            entity(as[Profile]) { profile =>
                                println(s"received update profile for $profileUuid : $profile")
                                complete(StatusCodes.OK)
                            }
                        },
                        // delete profile
                        delete {
                            println(s"delete profile id $profileUuid")
                            complete(StatusCodes.OK)
                        }
                    )
                }      
            )
        )
    )
}