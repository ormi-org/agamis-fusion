package io.ogdt.fusion.core.db.datastores.models.documents

import io.ogdt.fusion.core.db.models.documents.nested.file.{Metadata, Acl}

import scala.util.Try

import java.util.UUID

import reactivemongo.api.bson.{BSONDocument, BSONDocumentWriter, BSONDocumentReader, BSONObjectID}

final case class File(
    id: BSONObjectID,
    name: String,
    `type`: String,
    path: Option[String],
    parent: Option[BSONObjectID],
    chunkList: Option[List[UUID]],
    metadata: Metadata,
    versioned: Boolean,
    acl: Acl,
    owner: UUID
)

object File {
    implicit object FileReader extends BSONDocumentReader[File] {

        override def readDocument(doc: BSONDocument): Try[File] = for {
            id <- doc.getAsTry[BSONObjectID]("_id")
            `type` <- doc.getAsTry[String]("type")
            name <- doc.getAsTry[String]("name")
            path = doc.getAsOpt[String]("path")
            parent = doc.getAsOpt[BSONObjectID]("parent")
            chunkList = doc.getAsOpt[List[UUID]]("chunkList")
            metadata <- doc.getAsTry[Metadata]("metadata")
            versioned <- doc.getAsTry[Boolean]("versioned")
            acl <- doc.getAsTry[Acl]("acl")
            owner <- doc.getAsTry[UUID]("owner")
        } yield File(id, name, `type`, path, parent, chunkList, metadata, versioned, acl, owner)
    }

    implicit object FileWriter extends BSONDocumentWriter[File] {

        override def writeTry(file: File): Try[BSONDocument] = 
            scala.util.Success(BSONDocument(
                "_id" -> file.id,
                "name" -> file.name,
                "type" -> file.`type`,
                "parent" -> file.parent,
                "chunkList" -> file.chunkList,
                "metadata" -> file.metadata,
                "versioned" -> file.versioned,
                "acl" -> file.acl,
                "owner" -> file.owner,
            ))
    }
}