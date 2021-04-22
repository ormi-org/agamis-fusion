package io.ogdt.fusion.external.routes

import scala.concurrent.ExecutionContext
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{HttpEntity, ContentTypes, StatusCodes}

import java.util.UUID

class UserRoute {
    
    val routeAllUsers: Route = 
    path("api" / "user") {
        get {
            complete(StatusCodes.OK)
        } ~
        delete {
            complete(StatusCodes.OK)
        }
    }
    
    val routeUserId: Route = 
        path("api" / "user") {
            get {
                parameter("id".as[UUID]) { (userId: UUID) => 
                    println(s"get user id $userId")
                    complete(StatusCodes.OK)
                }
            } ~
            put {
                parameter("id".as[UUID]) { (userId: UUID) => 
                    println(s"put user id $userId")
                    complete(StatusCodes.OK)
                }
            } ~
            delete {
                parameter("id".as[UUID]) { (userId: UUID) => 
                    println(s"delete user id $userId")
                    complete(StatusCodes.OK)
                }
            }

        }

    val routeAllUser: Route = (path("api" / "user") & get) {
            complete(StatusCodes.OK)
        }

}