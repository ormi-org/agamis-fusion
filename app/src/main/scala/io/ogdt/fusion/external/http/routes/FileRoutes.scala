package io.ogdt.fusion.external.http.routes

import akka.actor.typed.ActorSystem
import akka.util.Timeout

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route

import scala.concurrent.duration._
import scala.concurrent.Future
import akka.actor.typed.ActorRef
import io.ogdt.fusion.external.http.actors.FileRepository
import io.ogdt.fusion.external.http.entities.FileJsonProtocol
import java.util.UUID
import io.ogdt.fusion.external.http.entities.File
import reactivemongo.api.bson.BSONObjectID
import io.ogdt.fusion.core.fs.lib.TreeManager

import scala.concurrent.ExecutionContext
import io.ogdt.fusion.core.db.wrappers.mongo.ReactiveMongoWrapper
import scala.util.Success
import scala.util.Failure
import io.ogdt.fusion.core.db.datastores.documents.FileStore
import scala.concurrent.Await
import akka.pattern.Patterns
import akka.actor.Status
import akka.http.javadsl.model.StatusCode
import akka.http.scaladsl.model.HttpResponse

class FileRoutes(buildFileRepository: ActorRef[FileRepository.Command])(implicit system: ActorSystem[_]) extends FileJsonProtocol{

    import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
    import akka.actor.typed.scaladsl.AskPattern.Askable

    // asking someone requires a timeout and a scheduler, if the timeout hits without response
    // the ask is failed with a TimeoutException
    implicit val timeout = Timeout(3.seconds)
    
    implicit val mongoWrapper = ReactiveMongoWrapper(system)

    lazy val routes: Route =
    concat(
        pathPrefix("init")(
            concat(
                get {
                    entity(as[File]) { file => 
                        val operationPerformed: Future[File] = 
                            buildFileRepository.ask(FileRepository.AddInitFile(file,_))
                            complete(file)
                    }
                }
            )  
        ),
        pathPrefix("files")(
            concat(
                // get all files
                get {
                    println(s"test file route")
                    complete(StatusCodes.OK)
                },
                // create file
                post {
                    entity(as[File]) { file =>
                        val operationPerformed: Future[FileRepository.Response] = 
                            buildFileRepository.ask(FileRepository.AddFile(file,_))
                            complete("OK")
                        onSuccess(operationPerformed) {
                            case FileRepository.OK  => complete("File added") 
                            case FileRepository.KO(reason) => complete(StatusCodes.InternalServerError -> reason)
                        }
                    }
                },
            )
        ),
        pathPrefix("file")(
            concat(
                //get by path
                get {
                    parameter("path".as[String]) { (path: String) => 
                        println(s"file path is $path")
                        complete(StatusCodes.OK)
                    }
                },
                pathPrefix(Segment) { fileId: String =>
                    concat(
                        //get by id
                        get {
                            println(s"get file id $fileId")
                            complete(StatusCodes.OK)
                        },
                        // update file
                        put {
                            entity(as[File]) { file =>
                                println(s"received update file for $fileId : $file")
                                complete(StatusCodes.OK)
                            }
                        },
                        // delete file
                        delete {
                            println(s"delete file id $fileId")
                            complete(StatusCodes.OK)
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