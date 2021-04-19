package io.ogdt.fusion.core.db.models.documents.nested.file.acl

import io.ogdt.fusion.core.db.models.documents.nested.file.acl.access.Rights

import scala.util.Try

import java.util.UUID

import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

final case class GroupAccess(
    groupId: UUID,
    rights: Rights
)

object GroupAccess {
    implicit object GroupAccessReader extends BSONDocumentReader[GroupAccess] {

        override def readDocument(doc: BSONDocument): Try[GroupAccess] = for {
            userId <- doc.getAsTry[UUID]("userId")
            rights <- doc.getAsTry[Rights]("rights")
        } yield GroupAccess(userId, rights)
    }

    implicit object GroupAccessWriter extends BSONDocumentWriter[GroupAccess] {

        override def writeTry(groupAccess: GroupAccess): Try[BSONDocument] =
            scala.util.Success(BSONDocument(
                "groupId" -> groupAccess.groupId,
                "rights" -> groupAccess.rights
            ))
    }
}