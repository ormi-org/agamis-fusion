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

import io.agamis.fusion.external.api.rest.dto.{Organization, OrganizationJsonProtocol}
import io.agamis.fusion.external.api.rest.actors.OrganizationRepository


/**
  * Class Organization Routes
  *
  * @param buildOrganizationRepository
  * @param system
  */
class OrganizationRoutes(buildOrganizationRepository: ActorRef[OrganizationRepository.Command])(implicit system: ActorSystem[_]) extends OrganizationJsonProtocol{

    import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
    import akka.actor.typed.scaladsl.AskPattern.Askable

    // asking someone requires a timeout and a scheduler, if the timeout hits without response
    // the ask is failed with a TimeoutException
    implicit val timeout = Timeout(3.seconds)

    lazy val routes: Route =
    concat(
        pathPrefix("organizations")(
            concat(
                // create organization
                post {
                    entity(as[Organization]) { organization =>
                        onComplete(buildOrganizationRepository.ask(OrganizationRepository.AddOrganization(organization,_))) {
                            case Success(response) => response match {
                                case OrganizationRepository.OK  => complete("Organizations added") 
                                case OrganizationRepository.KO(cause) => cause match {
                                    case _ => complete(StatusCodes.NotImplemented -> new Error(""))
                                }
                            }
                            case Failure(reason) => complete(StatusCodes.NotImplemented -> reason)
                        }
                    }
                },
            )
        ),
        pathPrefix("organization")(
            concat(
                pathPrefix(Segment) { organizationUuid: String =>
                    concat(
                        //get by id
                        get {
                            println(s"get organization uuid $organizationUuid")
                            onComplete(buildOrganizationRepository.ask(OrganizationRepository.GetOrganizationById(organizationUuid,_))) {
                                case Success(response) => response match {
                                    case OrganizationRepository.OK => complete(s"GET organization uuid : $organizationUuid")
                                    case OrganizationRepository.KO(cause) => cause match {
                                        case _ => complete(StatusCodes.InternalServerError -> new Error(""))
                                    }
                                }
                                case Failure(reason) => complete(StatusCodes.NotImplemented -> reason)    
                            }
                        },
                        // update organization
                        put {
                            entity(as[Organization]) { organization =>
                                println(s"received update organization for $organizationUuid : $organization")
                                onComplete(buildOrganizationRepository.ask(OrganizationRepository.UpdateOrganization(organizationUuid,_))) {
                                    case Success(response) => response match {
                                        case OrganizationRepository.OK => complete(s"UPDATE organization uuid : $organizationUuid")
                                        case OrganizationRepository.KO(cause) => cause match {
                                            case _ => complete(StatusCodes.InternalServerError -> new Error(""))
                                        }
                                    }
                                    case Failure(reason) => complete(StatusCodes.NotImplemented -> reason)    
                                }
                            }
                        },
                        // delete organization
                        delete {
                            println(s"delete organization id $organizationUuid")
                            onComplete(buildOrganizationRepository.ask(OrganizationRepository.DeleteOrganization(organizationUuid,_))) {
                                case Success(response) => response match {
                                    case OrganizationRepository.OK => complete(s"DELETE organization uuid : $organizationUuid")
                                    case OrganizationRepository.KO(cause) => cause match {
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