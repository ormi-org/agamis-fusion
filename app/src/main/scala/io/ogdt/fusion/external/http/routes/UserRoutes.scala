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

import io.ogdt.fusion.external.http.entities.User
import io.ogdt.fusion.external.http.actors.UserRepository
import io.ogdt.fusion.external.http.entities.UserJsonProtocol

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
                get {
                    println(s"test user route")
                    complete(StatusCodes.OK)
                },
                // create user
                post {
                    entity(as[User]) { user =>
                        val operationPerformed: Future[UserRepository.Response] = 
                            buildUserRepository.ask(UserRepository.AddUser(user,_))
                        onSuccess(operationPerformed) {                    
                            case UserRepository.OK  => complete("User added")
                            case UserRepository.KO(reason) => complete(StatusCodes.InternalServerError -> reason)
                        }
                    }
                },
            )
        ),
        pathPrefix("user")(
            concat(
                //get by username
                get {
                    parameter("username".as[String]) { (username: String) => 
                        println(s"username is $username")
                        complete(StatusCodes.OK)
                    }
                },
                pathPrefix(Segment) { userUuid: String =>
                    concat(
                        //get by id
                        get {
                            println(s"get user uuid $userUuid")
                            complete(StatusCodes.OK)
                        },
                        // update user
                        put {
                            entity(as[User]) { user =>
                                println(s"received update user for $userUuid : $user")
                                complete(StatusCodes.OK)
                            }
                        },
                        // delete user
                        delete {
                            println(s"delete user id $userUuid")
                            complete(StatusCodes.OK)
                        }
                    )
                }      
            )
        )
    )
}