package io.agamis.fusion.core.db.models.documents.nested.file.acl

import io.agamis.fusion.core.db.models.documents.nested.file.acl.access.Rights

import scala.util.Try

import java.util.UUID

import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

final case class ProfileAccess(
    profileId: UUID,
    rights: Rights
)

object ProfileAccess {
    implicit object ProfileAccessReader extends BSONDocumentReader[ProfileAccess] {

        override def readDocument(doc: BSONDocument): Try[ProfileAccess] = for {
            profileId <- doc.getAsTry[UUID]("profileId")
            rights <- doc.getAsTry[Rights]("rights")
        } yield ProfileAccess(profileId, rights)
    }

    implicit object ProfileAccessWriter extends BSONDocumentWriter[ProfileAccess] {

        override def writeTry(profileAccess: ProfileAccess): Try[BSONDocument] =
            scala.util.Success(BSONDocument(
                "profileId" -> profileAccess.profileId,
                "rights" -> profileAccess.rights
            ))
    }
}