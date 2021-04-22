package io.ogdt.fusion.external.routes

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{HttpEntity, ContentTypes, StatusCodes}
import akka.http.scaladsl.server.RequestContext
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

import java.util.UUID

import spray.json._

import io.ogdt.fusion.external.http.entities.File

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val fileFormat = jsonFormat[File](File)
}

object FileRouter extends Directives with JsonSupport {

    val routes =
        concat(
            //get by id
            get {
                parameter("id".as[UUID]) { (fileId: UUID) => 
                    println(s"get file id $fileId")
                    complete(StatusCodes.OK)
                }
            },
            //get by path
            get {
                parameter("path".as[String]) { (path: String) => 
                    println(s"path file id $path")
                    complete(StatusCodes.OK)
                }
            },
            // create file
            post {
                entity(as[File]) { file =>
                    println(s"received file $file")
                }
            },
            // update file
            put {
                entity(as[File]) { file =>
                    println(s"received update file $file")
                }
            },
            // delete file
            delete {
                parameter("id".as[UUID]) { (fileId: UUID) => 
                    println(s"delete file id $fileId")
                    complete(StatusCodes.OK)
                }
            }
        )

    // val routeAllFileVersions: Route = concat (
    //     (path("version") & get) {
    //         complete(StatusCodes.OK)
    //         }
    //     )

    // val routeFileWithVersion: Route = 
    // path("file") {
    //     get {
    //         parameter("id".as[UUID], "version".as[UUID]) { (fileId, versionId) => 
    //             println(s"get file id $fileId and version id $versionId")
    //             //getVersionByFileId(fileId, versionId)
    //             complete(StatusCodes.OK)
    //         }
    //     } ~
    //     delete {
    //         parameter("id".as[UUID], "version".as[UUID]) { (fileId, versionId) => 
    //             println(s"delete file id $fileId and version id $versionId")
    //             //getVersionByFileId(fileId, versionId)
    //             complete(StatusCodes.OK)
    //         }
    //     }
    // }    

}