package io.agamis.fusion.api.rest.routes


import scala.concurrent.duration._


import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes

import akka.util.Timeout

import akka.actor.typed.ActorSystem


/**
  * Class User Routes
  *
  * @param buildUserRepository
  * @param system
  */
class AuthenticationRoutes(implicit system: ActorSystem[_]) {

    


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