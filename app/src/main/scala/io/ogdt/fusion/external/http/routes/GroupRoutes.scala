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

import io.ogdt.fusion.external.http.entities.Group
import io.ogdt.fusion.external.http.actors.GroupRepository
import io.ogdt.fusion.external.http.entities.GroupJsonProtocol

class GroupRoutes(buildGroupRepository: ActorRef[GroupRepository.Command])(implicit system: ActorSystem[_]) extends GroupJsonProtocol{

    import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
    import akka.actor.typed.scaladsl.AskPattern.Askable

    // asking someone requires a timeout and a scheduler, if the timeout hits without response
    // the ask is failed with a TimeoutException
    implicit val timeout = Timeout(3.seconds)

    lazy val routes: Route =
    concat(
        pathPrefix("groups")(
            concat(
                // get all groups
                get {
                    println(s"test group route")
                    complete(StatusCodes.OK)
                },
                // create group
                post {
                    entity(as[Group]) { group =>
                        val operationPerformed: Future[GroupRepository.Response] = 
                            buildGroupRepository.ask(GroupRepository.AddGroup(group,_))
                        onSuccess(operationPerformed) {                    
                            case GroupRepository.OK  => complete("Group added")
                            case GroupRepository.KO(reason) => complete(StatusCodes.InternalServerError -> reason)
                        }
                    }
                },
            )
        ),
        pathPrefix("group")(
            concat(
                //get by name
                get {
                    parameter("name".as[String]) { (name: String) => 
                        println(s"name is $name")
                        complete(StatusCodes.OK)
                    }
                },
                pathPrefix(Segment) { groupUuid: String =>
                    concat(
                        //get by id
                        get {
                            println(s"get group uuid $groupUuid")
                            complete(StatusCodes.OK)
                        },
                        // update group
                        put {
                            entity(as[Group]) { group =>
                                println(s"received update group for $groupUuid : $group")
                                complete(StatusCodes.OK)
                            }
                        },
                        // delete group
                        delete {
                            println(s"delete group id $groupUuid")
                            complete(StatusCodes.OK)
                        }
                    )
                }      
            )
        )
    )

}