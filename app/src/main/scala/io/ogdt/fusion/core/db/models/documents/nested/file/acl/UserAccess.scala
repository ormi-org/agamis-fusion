package io.ogdt.fusion.core.db.models.documents.nested.file.acl

import io.ogdt.fusion.core.db.models.documents.nested.file.acl.access.Rights

import scala.util.Try

import java.util.UUID

import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

final case class UserAccess(
    userId: UUID,
    rights: Rights
)

object UserAccess {
    implicit object UserAccessReader extends BSONDocumentReader[UserAccess] {

        override def readDocument(doc: BSONDocument): Try[UserAccess] = for {
            userId <- doc.getAsTry[UUID]("userId")
            rights <- doc.getAsTry[Rights]("rights")
        } yield UserAccess(userId, rights)
    }

    implicit object UserAccessWriter extends BSONDocumentWriter[UserAccess] {

        override def writeTry(userAccess: UserAccess): Try[BSONDocument] =
            scala.util.Success(BSONDocument(
                "userId" -> userAccess.userId,
                "rights" -> userAccess.rights
            ))
    }
}