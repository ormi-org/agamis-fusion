package io.agamis.fusion.api.rest.routes

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

import io.agamis.fusion.api.rest.model.dto.user.UserDto
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.headers.RawHeader

/**
  * Class User Routes
  *
  * @param buildUserRepository
  * @param system
  */
class AuthenticationRoutes(implicit system: ActorSystem[_]) {

    import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
    import akka.actor.typed.scaladsl.AskPattern.Askable
    
    import io.agamis.fusion.api.rest.common.Jwt._

    import io.agamis.fusion.core.data.security.utils.HashPassword._

    // asking someone requires a timeout and a scheduler, if the timeout hits without response
    // the ask is failed with a TimeoutException
    implicit val timeout = Timeout(3.seconds)

    lazy val routes: Route =
    concat(
        pathPrefix("auth")( 
          complete(StatusCodes.NotImplemented)
        ), 
        pathPrefix("login")(
          complete(StatusCodes.NotImplemented)
        ), 
        pathPrefix("logout")(
          complete(StatusCodes.NotImplemented)
        )
    )
}