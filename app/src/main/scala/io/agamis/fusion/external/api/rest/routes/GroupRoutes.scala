package io.agamis.fusion.external.api.rest.routes

import scala.util.Success
import scala.util.Failure

import scala.concurrent.duration._
import scala.concurrent.Future

import java.util.UUID

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{HttpEntity, ContentTypes, StatusCodes}

import akka.util.Timeout

import akka.actor.typed.{ActorSystem, ActorRef}

import io.agamis.fusion.external.api.rest.dto.Group
import io.agamis.fusion.external.api.rest.actors.GroupRepository
import io.agamis.fusion.external.api.rest.dto.GroupJsonProtocol

/**
  * Class Group Routes
  *
  * @param buildGroupRepository
  * @param system
  */
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
                // get {
                //     println(s"test group route")
                //     complete(StatusCodes.OK)
                // },
                // create group
                post {
                    entity(as[Group]) { group =>
                        onComplete(buildGroupRepository.ask(GroupRepository.AddGroup(group,_))) {
                            case Success(value) => complete(StatusCodes.OK)
                            case Failure(reason) => complete(StatusCodes.NotImplemented)
                        }
                    }
                }
            )
        ),
        pathPrefix("group")(
            concat(
                //get by name
                get {
                    parameter("name".as[String]) { (name: String) => 
                    println(s"name is $name")
                    onComplete(buildGroupRepository.ask(GroupRepository.GetGroupByName(name,_))) {
                        case Success(group) => complete(StatusCodes.OK)
                        case Failure(reason) => reason match {
                            case _ => complete(StatusCodes.NotImplemented -> reason)
                            }
                        }
                    }
                },
                pathPrefix(Segment) { groupUuid: String =>
                    concat(
                        //get by id
                        get {
                            println(s"get group uuid $groupUuid")
                            onComplete(buildGroupRepository.ask(GroupRepository.GetGroupById(groupUuid,_))) {
                                case Success(group) => complete(StatusCodes.OK)
                                case Failure(reason) => reason match {
                                    case _ => complete(StatusCodes.NotImplemented -> reason)
                                }
                            }
                        },
                        // update group
                        put {
                            entity(as[Group]) { group =>
                                println(s"received update group for $groupUuid : $group")
                                onComplete(buildGroupRepository.ask(GroupRepository.UpdateGroup(group,_))) {
                                    case Success(response) => response match {
                                        case GroupRepository.OK  => complete("Group updated") 
                                        case GroupRepository.KO(cause) => cause match {
                                            case _ => complete(StatusCodes.NotImplemented -> new Error(""))
                                        }
                                    }
                                    case Failure(reason) => complete(StatusCodes.NotImplemented -> reason)
                                }
                            }
                        },
                        // delete group
                        delete {
                            println(s"delete group id $groupUuid")
                            entity(as[Group]) { group =>
                            onComplete(buildGroupRepository.ask(GroupRepository.DeleteGroup(group,_))) {
                                case Success(response) => response match {
                                    case GroupRepository.OK  => complete("Group deleted") 
                                    case GroupRepository.KO(cause) => cause match {
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