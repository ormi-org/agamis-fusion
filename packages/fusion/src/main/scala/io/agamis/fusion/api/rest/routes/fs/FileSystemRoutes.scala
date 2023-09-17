package io.agamis.fusion.api.rest.routes

import scala.util.Success
import scala.util.Failure
import scala.concurrent.duration._
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import io.agamis.fusion.api.rest.model.dto.filesystem.{FileSystemJsonSupport, FileSystemDto}

/**
  * Class File System Routes
  *
  * @param system
  */
class FileSystemRoutes(implicit system: ActorSystem[_]) extends FileSystemJsonSupport {
    
    import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
    import akka.actor.typed.scaladsl.AskPattern.Askable

    implicit val timeout = Timeout(3.seconds)

    lazy val routes: Route =
      concat(
        pathPrefix("file-systems")(
          concat(
            // get all fileSystems
            get {
              complete(StatusCodes.NotImplemented)
            },
            // create fileSystem
            post {
              entity(as[FileSystemDto]) { fileSystem =>
                complete(StatusCodes.NotImplemented)
              }
            },
          )
        ),
        pathPrefix("file-system")(
          concat(
            //get by id
            get {
              path(Segment) { id: String =>
                complete(StatusCodes.NotImplemented)
              }
            },
            // update fileSystem
            put {
              path(Segment) { id: String =>
                entity(as[FileSystemDto]) { fileSystem =>
                  complete(StatusCodes.NotImplemented)
                }
              }
            },
            // delete fileSystem
            delete {
              path(Segment) { id: String =>
                complete(StatusCodes.NotImplemented)
              }
            }
          )
        )
      )
}