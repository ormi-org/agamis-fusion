package io.agamis.fusion.external.api.rest.routes

import scala.util.Success
import scala.util.Failure
import scala.concurrent.duration._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import io.agamis.fusion.external.api.rest.actors.FileRepository
import akka.http.scaladsl.model.HttpResponse
import io.agamis.fusion.external.api.rest.dto.fs.file.FileDto
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

/**
  * Class File Routes 
  *
  * @param buildFileRepository
  * @param system
  */
class FileRoutes(buildFileRepository: ActorRef[FileRepository.Command])(implicit system: ActorSystem[_]) {

    import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
    import akka.actor.typed.scaladsl.AskPattern.Askable
    import io.agamis.fusion.external.api.rest.dto.fs.file.FileDtoJsonProtocol._

    implicit val timeout: Timeout = Timeout(3.seconds)

    lazy val routes: Route =
    concat(
        pathPrefix(Segment) { fsId: String =>
            concat(
                pathPrefix("files")(
                    concat(
                        // get all files
                        // get {
                            //println(s"test file route")
                            // complete(buildFileRepository.ask(FileRepository.GetAllFileById(id,_)).map(_ => StatusCodes.OK))
                        // },
                        // create file
                        post {
                            entity(as[FileDto]) { file =>
                                onComplete(buildFileRepository.ask(FileRepository.AddFile(file,_))) {
                                    case Success(result) => result match {
                                        case FileRepository.OK => complete(HttpResponse(StatusCodes.OK, entity = "File created"))
                                        case FileRepository.KO(cause) => 
                                            cause match {
                                                case _ => complete(HttpResponse(StatusCodes.InternalServerError, entity = cause))
                                            }
                                    }  
                                    case Failure(reason) => complete(HttpResponse(StatusCodes.InternalServerError, entity = reason.toString))
                                }
                            }
                        },
                        // delete {
                        //     entity(as[List[File]]) { files =>
                        //         onComplete(buildFileRepository.ask(FileRepository.DeleteManyFile(files,_))) {
                        //             case Success(response) => response match {
                        //                 case FileRepository.OK  => complete("File delete") 
                        //                 case FileRepository.KO(cause) => 
                        //                     cause match {
                        //                         case _ => complete(HttpResponse(StatusCodes.InternalServerError, entity = cause))
                        //                     }
                        //             }
                        //             case Failure(reason) => complete(HttpResponse(StatusCodes.InternalServerError, entity = reason.toString()))
                        //         }
                        //     }
                        // }
                    )
                ),
                pathPrefix("file")(
                    concat(
                        // get by path
                        pathPrefix("path" / Segment) { path: String => 
                            get {
                                onComplete(buildFileRepository.ask(FileRepository.GetFileByPath(path,_))) {
                                    case Success(file) => complete(StatusCodes.OK -> file)
                                    case Failure(reason) => reason match {
                                        case _ => complete(HttpResponse(StatusCodes.InternalServerError, entity = reason.toString()))
                                    }
                                }
                            }
                        },
                        pathPrefix("id" / Segment) { fileId: String =>
                            concat(
                                //get by id
                                get {
                                    onComplete(buildFileRepository.ask(FileRepository.GetFileById(fileId,_))) {
                                        case Success(file) => complete(StatusCodes.OK -> file)
                                        case Failure(reason) => reason match {
                                            case _ => complete(HttpResponse(StatusCodes.InternalServerError, entity = reason.toString()))
                                        }
                                    }
                                },
                                //update file
                                put {
                                    entity(as[FileDto]) { file =>
                                        println(s"received update file for $fileId : $file")
                                        onComplete(buildFileRepository.ask(FileRepository.UpdateFile(file,_))) {
                                            case Success(response) => response match {
                                                case FileRepository.OK  => complete("File updated") 
                                                case FileRepository.KO(cause) => cause match {
                                                    case _ => complete(HttpResponse(StatusCodes.InternalServerError, entity = cause))
                                                }
                                            }
                                            case Failure(reason) => complete(StatusCodes.InternalServerError -> reason)
                                        }
                                    }
                                },
                                // delete file
                                delete {
                                    println(s"delete file id $fileId")
                                    onComplete(buildFileRepository.ask(FileRepository.DeleteFile(fileId,_))) {
                                        case Success(response) => response match {
                                            case FileRepository.OK  => complete("File deleted")
                                            case FileRepository.KO(cause) => cause match {
                                                case _ => complete(HttpResponse(StatusCodes.InternalServerError, entity = cause))
                                            }
                                        }
                                        case Failure(reason) => complete(StatusCodes.InternalServerError -> reason)
                                    }
                                }
                            )
                        }
                        // get {
                        //     parameter("id".as[UUID]) { (fileId: UUID) => 
                        //         println(s"get file id $fileId")
                        //         complete(StatusCodes.OK)
                        //     }
                        // },                
                    )
                )
            )
        }
    )
}