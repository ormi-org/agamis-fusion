package io.agamis.fusion.core.db.models.documents.file.acl.access

import io.agamis.fusion.core.db.models.documents.file.acl.access
import io.agamis.fusion.core.db.models.documents.file.acl.access.rights.Rights
import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

import java.util.UUID
import scala.util.Try

final case class GroupAccess(
    groupId: UUID,
    rights: Rights
)

object GroupAccess {
    implicit object GroupAccessReader extends BSONDocumentReader[GroupAccess] {

        override def readDocument(doc: BSONDocument): Try[GroupAccess] = for {
            userId <- doc.getAsTry[UUID]("userId")
            rights <- doc.getAsTry[Rights]("rights")
        } yield access.GroupAccess(userId, rights)
    }

    implicit object GroupAccessWriter extends BSONDocumentWriter[GroupAccess] {

        override def writeTry(groupAccess: GroupAccess): Try[BSONDocument] =
            scala.util.Success(BSONDocument(
                "groupId" -> groupAccess.groupId,
                "rights" -> groupAccess.rights
            ))
    }
}