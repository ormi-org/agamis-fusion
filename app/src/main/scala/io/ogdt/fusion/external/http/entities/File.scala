package io.ogdt.fusion.external.http.entities

import reactivemongo.api.bson.BSONObjectID

import java.util.UUID
import java.time.Instant

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

import spray.json._
import spray.json.{DefaultJsonProtocol, RootJsonFormat, DeserializationException, JsValue, JsString}

import io.ogdt.fusion.core.db.models.documents.{File => FileDocument}

import io.ogdt.fusion.external.http.entities.File.DIRECTORY
import io.ogdt.fusion.external.http.entities.File.FILE

import io.ogdt.fusion.external.http.entities.common.JsonFormatters._

import io.ogdt.fusion.external.http.entities.nested.file.{Metadata, Acl}
import io.ogdt.fusion.external.http.entities.nested.file.MetadataJsonProtocol._
import io.ogdt.fusion.external.http.entities.nested.file.metadata.FusionXmlMeta
import io.ogdt.fusion.external.http.entities.nested.file.AclJsonProtocol._
import io.ogdt.fusion.external.http.entities.nested.file.acl.access.Rights
import scala.util.{Success, Failure}


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
            // f.id.get,
            f.id match {
                case Some(value) => value
                case None => BSONObjectID.generate()
            }, 
            f.name,
            f.`type` match {
                case DIRECTORY => FileDocument.DIRECTORY
                case FILE => FileDocument.FILE
            },
            // f.path,
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
        def write(f: File) = {
            var values: List[JsField] = List()
            f.id match {
                case Some(id) => values :+= ("id" -> JsString(id.stringify))
                case None => BSONObjectID.generate()
            }
            values :+= ("name" -> JsString(f.name))
            f.`type` match {
                case DIRECTORY => values :+= ("type" -> JsString("DIRECTORY"))
                case FILE => values :+= ("type" -> JsString("FILE"))
            }
            f.path match {
                case Some(path) => values :+= ("path" -> JsString(path))
                case None => 
            }
            f.parent match {
                case Some(parentId) => values :+= ("parent" -> JsString(parentId.stringify))
                case None =>
            }
            f.chunkList match {
                case Some(chunkList) => values :+= ("chunkList" -> JsArray(chunkList.map(chunkId => JsString(chunkId.toString()))))
                case None =>
            }
            values :+= ("metadata" -> f.metadata.toJson)
            f.versioned match {
                case Some(versioned) => values :+= ("versioned" -> JsBoolean(versioned))
                case None =>
            }
            values :+= ("acl" -> f.acl.toJson)
            values :+= ("owner" -> JsString(f.owner.toString()))
            JsObject(values:_*)
        }
        
        def read(json: JsValue): File = {
            val jsFileObject: JsObject = json.asJsObject
            jsFileObject.getFields("name", "type", "metadata", "acl", "owner") match {
                case Seq(
                    name,
                    fileType,
                    metadata,
                    acl,
                    owner
                ) => {
                    new File(
                        jsFileObject.fields.get("id").map(_.convertTo[String]) match {
                            case Some(id) => BSONObjectID.parse(id) match {
                                case Success(value) => Some(value)
                                case Failure(cause) => throw new Exception("bla bla bla", cause) // TODO : changer pour une custom
                            }
                            case None => Some(BSONObjectID.generate())
                        },
                        name.convertTo[String],
                        fileType.convertTo[String] match {
                            case "DIRECTORY" => DIRECTORY
                            case "FILE" => FILE
                        },
                        jsFileObject.fields.get("path").map(_.convertTo[String]) match {
                            case Some(value) => Some(value)
                            case None => Some("/")
                        },
                        jsFileObject.fields.get("parent").map(_.convertTo[String]) match {
                            case Some(parent) => BSONObjectID.parse(parent) match {
                                case Success(value) => Some(value)
                                case Failure(cause) => throw new Exception("bla bla bla", cause) // TODO : changer pour une custom
                            }
                            case None => None
                        },
                        Some(jsFileObject.fields.get("chunkList").map(_.convertTo[String]).toList.map(
                            chunkId => UUID.fromString(chunkId)
                        )),
                        metadata.convertTo[Metadata],
                        jsFileObject.fields.get("versioned").map(_.convertTo[Boolean]),
                        acl.convertTo[Acl],
                        UUID.fromString(owner.convertTo[String])
                    )
                }
                case other => throw new DeserializationException("Cannot deserialize File: File expected but got invalid input. Raw input: " + other)
            }
        }
    }
}

