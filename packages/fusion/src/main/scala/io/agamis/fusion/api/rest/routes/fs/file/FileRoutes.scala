package io.agamis.fusion.api.rest.routes

import scala.concurrent.duration._
import akka.actor.typed.ActorSystem
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import io.agamis.fusion.api.rest.model.dto.fs.file.FileDto
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

/** Class File Routes
  *
  * @param system
  */
class FileRoutes(implicit system: ActorSystem[_]) {

  import io.agamis.fusion.api.rest.model.dto.fs.file.FileDtoJsonProtocol._

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
            parameter("path".as[String]) { _ =>
              complete(StatusCodes.NotImplemented)
            },
            parameter("id".as[String]) { _ =>
              // muste verify id is a BSONObjectID
              complete(StatusCodes.NotImplemented)
            }
          )
        }
      ),
      pathPrefix("make")(
        post {
          entity(as[FileDto]) { _ =>
            complete(StatusCodes.NotImplemented)
          }
        }
      ),
      pathPrefix("remove")(
        get {
          concat(
            parameter("path".as[String]) { _ =>
              complete(StatusCodes.NotImplemented)
            },
            path(Segment) { _: String =>
              complete(StatusCodes.NotImplemented)
            }
          )
        }
      )
    )
}
