package io.agamis.fusion.external.api.rest.routes

import scala.util.Success
import scala.util.Failure
import scala.concurrent.duration._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.HttpResponse
import io.agamis.fusion.external.api.rest.dto.fs.file.FileDto
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

/** Class File Routes
  *
  * @param system
  */
class FileRoutes(implicit system: ActorSystem[_]) {

  import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
  import akka.actor.typed.scaladsl.AskPattern.Askable
  import io.agamis.fusion.external.api.rest.dto.fs.file.FileDtoJsonProtocol._

  implicit val timeout: Timeout = Timeout(3.seconds)

  /** Those routes are located under fileSystem path file-systems/{fs_id}/
    * /!\ From this point, API but implements its own set of actions to reflect FusionFS functionnalities
    * 
    * typical path is .../{operation}?params
    * 
    * implemented operations:
    *   - list:   will list files and directories located under this path
    *     params:
    *       - path: select context based on file path
    *       - id:   select context based on file uuid
    *       - ...
    *   - make:   will create a new file or directory suiting provided
    *   - remove: will delete an existing file or 
    *     params:
    *       - path: select file based on file path
    *       - id:   select file based on file uuid
    *       - ...
    *   - write:  will write data to a file via its merging queue
    *   - read:   will read data from a file
    */
  lazy val routes: Route =
    concat(
      pathPrefix("list")(
        get {
          concat(
            parameter("path".as[String]) { path =>
              complete(StatusCodes.NotImplemented)
            },
            parameter("id".as[String]) { path =>
              // muste verify id is a BSONObjectID
              complete(StatusCodes.NotImplemented)
            }
          )
        }
      ),
      pathPrefix("make")(
        post {
          entity(as[FileDto]) { file =>
            complete(StatusCodes.NotImplemented)
          }
        }
      ),
      pathPrefix("remove")(
        get {
          concat(
            parameter("path".as[String]) { path =>
              complete(StatusCodes.NotImplemented)
            },
            path(Segment) { id: String =>
              complete(StatusCodes.NotImplemented)
            }
          )
        }
      )
    )
}
