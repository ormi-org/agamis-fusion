package io.ogdt.fusion.core.db.datastores.models.documents

import io.ogdt.fusion.core.db.models.documents.nested.file.{Metadata, Acl}

import scala.util.Try

import java.util.UUID

import reactivemongo.api.bson.{
    BSONDocument,
    BSONDocumentWriter,
    BSONDocumentReader,
    BSONObjectID,
    BSONString,
    BSONValue,
    BSONReader
}

final case class File(
    id: BSONObjectID,
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

    implicit object FileReader extends BSONDocumentReader[File] {

        implicit object FileTypeReader extends BSONReader[FileType] {
            override def readTry(value: BSONValue): Try[FileType] = {
                value match {
                    case BSONString("FILE") => Try(FILE)
                    case BSONString("DIRECTORY") => Try(DIRECTORY)
                    case _ => throw new Exception("Couldn't get implicit value for FileType")
                }
            }
        }

        override def readDocument(doc: BSONDocument): Try[File] = for {
            id <- doc.getAsTry[BSONObjectID]("_id")
            `type` <- doc.getAsTry[FileType]("type")
            name <- doc.getAsTry[String]("name")
            path = doc.getAsOpt[String]("path")
            parent = doc.getAsOpt[BSONObjectID]("parent")
            chunkList = doc.getAsOpt[List[UUID]]("chunkList")
            metadata <- doc.getAsTry[Metadata]("metadata")
            versioned = doc.getAsOpt[Boolean]("versioned")
            acl <- doc.getAsTry[Acl]("acl")
            owner <- doc.getAsTry[UUID]("owner")
        } yield File(id, name, `type`, path, parent, chunkList, metadata, versioned, acl, owner)
    }

    implicit object FileWriter extends BSONDocumentWriter[File] {

        override def writeTry(file: File): Try[BSONDocument] = 
            scala.util.Success(BSONDocument(
                "_id" -> file.id,
                "name" -> file.name,
                "type" -> file.`type`.toString(),
                "parent" -> file.parent,
                "chunkList" -> file.chunkList,
                "metadata" -> file.metadata,
                "versioned" -> file.versioned,
                "acl" -> file.acl,
                "owner" -> file.owner,
            ))
    }
}