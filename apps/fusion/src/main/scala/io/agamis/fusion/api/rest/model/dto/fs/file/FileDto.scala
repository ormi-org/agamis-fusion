package io.agamis.fusion.api.rest.model.dto.fs.file

import io.agamis.fusion.api.rest.model.dto.fs.file.acl.AclDto
import io.agamis.fusion.api.rest.model.dto.fs.file.acl.AclDtoJsonProtocol._
import io.agamis.fusion.api.rest.model.dto.fs.file.exceptions.InvalidObjectId
import io.agamis.fusion.api.rest.model.dto.fs.file.metadata.MetadataDto
import io.agamis.fusion.api.rest.model.dto.fs.file.metadata.MetadataDtoJsonProtocol._
import io.agamis.fusion.core.db.models.documents.file.File
import reactivemongo.api.bson.BSONObjectID
import spray.json._

import java.util.UUID
import scala.util.Failure
import scala.util.Success

final case class FileDto(
    id: Option[String],
    name: String,
    `type`: String,
    path: Option[String],
    parent: Option[String],
    chunkList: Option[List[String]],
    metadata: MetadataDto,
    versioned: Option[Boolean],
    acl: AclDto,
    owner: String
)

object FileDto {

    sealed trait FileType

    case object FILE extends FileType {
        override def toString: String = {
            "FILE"
        }
    }

    case object DIRECTORY extends FileType {
        override def toString: String = {
            "DIRECTORY"
        }
    }

    implicit def apply(dto: FileDto): File = {
        File(
          dto.id.orNull match {
              case id: String =>
                  BSONObjectID.parse(id) match {
                      case Success(id) => id
                      case Failure(cause) =>
                          throw InvalidObjectId(cause.getMessage)
                  }
              case null => throw InvalidObjectId()
          },
          dto.name,
          dto.`type` match {
              case "DIRECTORY" => File.DIRECTORY
              case "FILE"      => File.FILE
          },
          dto.path,
          dto.parent.orNull match {
              case parent: String =>
                  BSONObjectID.parse(parent) match {
                      case Success(parent) => Some(parent)
                      case Failure(cause) =>
                          throw InvalidObjectId(cause.getMessage)
                  }
              case null => throw InvalidObjectId()
          },
          dto.chunkList.orNull match {
              case chunks: List[String] =>
                  Some(chunks.map(c => UUID.fromString(c)))
              case null => None
          },
          dto.metadata,
          dto.versioned,
          dto.acl,
          UUID.fromString(dto.owner)
        )
    }

    implicit def apply(doc: File): FileDto = {
        FileDto(
          Some(doc.id.toString),
          doc.name,
          doc.fType match {
              case File.DIRECTORY => DIRECTORY.toString
              case File.FILE      => FILE.toString
          },
          doc.path,
          doc.parent.orNull match {
              case parent: BSONObjectID => Some(parent.toString)
              case null                 => None
          },
          doc.chunkList.orNull match {
              case chunks: List[UUID] => Some(chunks.map(c => c.toString))
              case null               => None
          },
          doc.metadata,
          doc.versioned,
          doc.acl,
          doc.owner.toString
        )
    }
}

object FileDtoJsonProtocol extends DefaultJsonProtocol {

    implicit val fileFormat: RootJsonFormat[FileDto] = jsonFormat10(
      FileDto.apply
    )
}
