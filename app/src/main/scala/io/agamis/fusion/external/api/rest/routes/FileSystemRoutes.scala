package io.agamis.fusion.external.api.rest.routes

import scala.util.Success
import scala.util.Failure
import scala.concurrent.duration._
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import io.agamis.fusion.external.api.rest.actors.FileSystemRepository
import io.agamis.fusion.external.api.rest.dto.request.{FileSystemInitRequest, FileSystemJsonProtocol}

/**
  * Class File System Routes
  *
  * @param buildFileSystemRepository
  * @param system
  */
class FileSystemRoutes(buildFileSystemRepository: ActorRef[FileSystemRepository.Command])(implicit system: ActorSystem[_]) extends FileSystemJsonProtocol {
    
    import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
    import akka.actor.typed.scaladsl.AskPattern.Askable

    implicit val timeout = Timeout(3.seconds)

    lazy val routes: Route =
        concat(
        pathPrefix("init")(
            concat(
                post {
                    entity(as[FileSystemInitRequest]) { fsRequest =>
                        onComplete(buildFileSystemRepository.ask(FileSystemRepository.InitFileSystem(fsRequest,_))) {
                            case Success(response) => response match {
                                case FileSystemRepository.OK => complete("File System added")
                                case FileSystemRepository.KO(cause) => cause match {
                                    case _ => complete(StatusCodes.InternalServerError -> new Error(""))
                                }
                            }
                            case Failure(reason) => complete(StatusCodes.InternalServerError -> reason)
                        }
                    }
                }, 
            )  
        ),
    )

}