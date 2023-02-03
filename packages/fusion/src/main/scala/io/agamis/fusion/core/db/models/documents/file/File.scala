package io.agamis.fusion.core.db.models.documents.file

import io.agamis.fusion.core.db.models.documents.file.acl.Acl
import io.agamis.fusion.core.db.models.documents.file.metadata.Metadata
import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID, BSONReader, BSONString}

import java.util.UUID
import scala.util.Try

final case class File(
    id: BSONObjectID,
    name: String,
    fType: File.FileType,
    @transient path: Option[String],
    parent: Option[BSONObjectID],
    chunkList: Option[List[UUID]],
    metadata: Metadata,
    versioned: Option[Boolean],
    acl: Acl,
    owner: UUID
) {
    def isDirectory: Boolean = {
        this.fType == File.DIRECTORY
    }
}

object File {

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

    implicit object FileReader extends BSONDocumentReader[File] {

        implicit val fileTypeReader: BSONReader[FileType] = {
            BSONReader.from[FileType] {
                case BSONString("FILE") => Try(FILE)
                case BSONString("DIRECTORY") => Try(DIRECTORY)
                case _ => throw new Exception("Couldn't get implicit value for FileType")
            }
        }

        override def readDocument(doc: BSONDocument): Try[File] = for {
            id <- doc.getAsTry[BSONObjectID]("_id")
            name <- doc.getAsTry[String]("name")
            fileType <- doc.getAsTry[FileType]("type")
            path = doc.getAsOpt[String]("path")
            parent = doc.getAsOpt[BSONObjectID]("parent")
            chunkList = doc.getAsOpt[List[UUID]]("chunkList")
            metadata <- doc.getAsTry[Metadata]("metadata")
            versioned = doc.getAsOpt[Boolean]("versioned")
            acl <- doc.getAsTry[Acl]("acl")
            owner <- doc.getAsTry[UUID]("owner")
        } yield File(id, name, fileType, path, parent, chunkList, metadata, versioned, acl, owner)
    }

    implicit object FileWriter extends BSONDocumentWriter[File] {

        override def writeTry(file: File): Try[BSONDocument] = 
            scala.util.Success(BSONDocument(
                "_id" -> file.id,
                "name" -> file.name,
                "type" -> file.fType.toString,
                "parent" -> file.parent,
                "chunkList" -> file.chunkList,
                "metadata" -> file.metadata,
                "versioned" -> file.versioned,
                "acl" -> file.acl,
                "owner" -> file.owner,
            ))
    }
}