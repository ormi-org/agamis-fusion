package io.agamis.fusion.external.http.routes

import scala.util.Success
import scala.util.Failure

import scala.concurrent.duration._

import java.util.UUID

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{HttpEntity, ContentTypes, StatusCodes}

import akka.util.Timeout

import io.agamis.fusion.external.http.actors.PermissionRepository

import io.agamis.fusion.external.http.entities.{Permission, PermissionJsonProtocol}

import akka.actor.typed.{ActorSystem, ActorRef}
import scala.concurrent.Future
import io.agamis.fusion.external.http.entities.Organization

/**
  * Class Permissions Routes 
  *
  * @param buildPermissionRepository
  * @param system
  */
class PermissionRoutes(buildPermissionRepository: ActorRef[PermissionRepository.Command])(implicit system: ActorSystem[_]) extends PermissionJsonProtocol {

    import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
    import akka.actor.typed.scaladsl.AskPattern.Askable

    implicit val timeout = Timeout(3.seconds)

    lazy val routes: Route =
    concat(
        pathPrefix("permissions")(
            concat(
            )
        )
    )

}