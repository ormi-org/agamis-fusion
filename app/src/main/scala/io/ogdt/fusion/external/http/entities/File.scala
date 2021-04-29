package io.ogdt.fusion.external.http.entities

import reactivemongo.api.bson.BSONObjectID
import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

import io.ogdt.fusion.external.http.entities.nested.file.{Metadata, Acl}

import io.ogdt.fusion.external.http.entities.nested.file.MetadataJsonProtocol._
import io.ogdt.fusion.external.http.entities.nested.file.AclJsonProtocol._

import io.ogdt.fusion.external.http.entities.common.JsonFormatters._

import spray.json.{DefaultJsonProtocol, RootJsonFormat, DeserializationException, JsValue, JsString}

import io.ogdt.fusion.core.db.models.documents.{File => FileDocument}

import io.ogdt.fusion.external.http.entities.File.DIRECTORY
import io.ogdt.fusion.external.http.entities.File.FILE

import spray.json._

final case class File(
    id: Option[BSONObjectID],
    name: String,
    `type`: File.FileType,
    path: Option[String],
    parent: Option[BSONObjectID],
    chunkList: Option[List[UUID]],
    metadata: Metadata,
    versioned: Option[Boolean],
    acl: Acl,
    owner: UUID
) 


object File {
    
    sealed trait FileType
    case object FILE extends FileType {
        override def toString(): String = {
            "FILE"
        }
    }
    case object DIRECTORY extends FileType {
        override def toString(): String = {
            "DIRECTORY"
        }
    }

    implicit def fileToFileDocument(f: File): FileDocument = {
        FileDocument(
            f.id.get,
            f.name,
            f.`type` match {
                case DIRECTORY => FileDocument.DIRECTORY
                case FILE => FileDocument.FILE
            },
            f.path,
            f.parent,
            f.chunkList,
            f.metadata,
            f.versioned,
            f.acl,
            f.owner
        )
    }

    implicit def documentToFile(doc: FileDocument): File = {
        File(
            Some(doc.id),
            doc.name,
            doc.`type` match {
                case FileDocument.DIRECTORY => DIRECTORY
                case FileDocument.FILE => FILE
            },
            doc.path,
            doc.parent,
            doc.chunkList,
            doc.metadata,
            doc.versioned,
            doc.acl,
            doc.owner       
        )
    }
}

trait FileJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {

    import io.ogdt.fusion.external.http.actors.FileRepository._

    implicit object StatusFormat extends RootJsonFormat[Status] {
        def write(status: Status): JsValue = status match {
            case Failed     => JsString("Failed")
            case Successful => JsString("Success") 
        }

        def read(json: JsValue): Status = json match {
            case JsString("Failed")     => Failed
            case JsString("Successful") => Successful
            case _                      => throw new DeserializationException("Status unexpected")
        }
    }

    implicit object FileJsonFormat extends RootJsonFormat[File] {
        def write(f: File) = JsObject(
            "id" -> JsString(f.id.getOrElse(null).stringify),
            "name" -> JsString(f.name),
            "type" -> JsString(
                f.`type` match {
                    case DIRECTORY => "DIRECTORY"
                    case FILE => "FILE"
                }
            ),
            "path" -> JsString(f.path.getOrElse(null)),
            "parent" -> JsString(f.parent.getOrElse(null).stringify),
            "chunkList" -> JsArray(f.chunkList.getOrElse(null).map(chunkId => JsString(chunkId.toString()))),
            "metadata" -> f.metadata.toJson,
            "versioned" -> JsBoolean(f.versioned.get),
            "acl" -> f.acl.toJson,
            "owner" -> JsString(f.owner.toString()),
        )
        def read(json: JsValue): File = {
            val jsFileObject: JsObject = json.asJsObject

            jsFileObject.getFields("name", "type", "metadata", "versionned", "acl", "owner") match {
                case Seq(
                    JsString(name),
                    JsString(fileType),
                    metadata,
                    JsBoolean(versioned),
                    acl,
                    JsString(owner)
                ) => new File(
                    Some(BSONObjectID.parse(jsFileObject.fields.get("id").map(_.convertTo[String]).getOrElse(null)).getOrElse(null)),
                    name,
                    fileType match {
                        case "DIRECTORY" => DIRECTORY
                        case "FILE" => FILE
                    },
                    jsFileObject.fields.get("path").map(_.convertTo[String]),
                    Some(BSONObjectID.parse(jsFileObject.fields.get("parent").map(_.convertTo[String]).getOrElse(null)).getOrElse(null)),
                    Some(jsFileObject.fields.get("chunkList").map(_.convertTo[JsArray]).toList.map(
                        chunkId => UUID.fromString(chunkId.toString())
                    )),
                    metadata.convertTo[Metadata],
                    Some(versioned),
                    acl.convertTo[Acl], 
                    UUID.fromString(owner.toString())
                )
                case other => throw new DeserializationException("Cannot deserialize File: File expected but got invalid input. Raw input: " + other)
            }
        }
    }
}

