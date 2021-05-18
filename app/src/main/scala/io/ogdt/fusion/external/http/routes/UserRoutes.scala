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

/**
  * Class User Routes
  *
  * @param buildUserRepository
  * @param system
  */
class UserRoutes(buildUserRepository: ActorRef[UserRepository.Command])(implicit system: ActorSystem[_]) extends UserJsonProtocol{

    import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
    import akka.actor.typed.scaladsl.AskPattern.Askable

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
                        onComplete(buildUserRepository.ask(UserRepository.AddUser(user,_))) {
                            case Success(value) => complete(StatusCodes.OK)
                            case Failure(reason) => complete(StatusCodes.NotImplemented)
                        }
                    }
                }
            )
        ),
        pathPrefix("user")(
            concat(
                //get by name
                get {
                    parameter("name".as[String]) { (name: String) => 
                    println(s"name is $name")
                    onComplete(buildUserRepository.ask(UserRepository.GetUserByPath(name,_))) {
                        case Success(user) => complete(StatusCodes.OK)
                        case Failure(reason) => reason match {
                            case _ => complete(StatusCodes.NotImplemented -> reason)
                            }
                        }
                    }
                },
                pathPrefix(Segment) { userUuid: String =>
                    concat(
                        //get by id
                        get {
                            println(s"get user uuid $userUuid")
                            onComplete(buildUserRepository.ask(UserRepository.GetUserById(userUuid,_))) {
                                case Success(user) => complete(StatusCodes.OK)
                                case Failure(reason) => reason match {
                                    case _ => complete(StatusCodes.NotImplemented -> reason)
                                }
                            }
                        },
                        // update user
                        put {
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
                        },
                        // delete user
                        delete {
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
                    )
                }      
            )
        )
    )
}