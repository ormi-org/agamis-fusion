package io.agamis.fusion.core.db.models.documents.file.acl

import io.agamis.fusion.core.db.models.documents.file.acl.access.{GroupAccess, ProfileAccess}
import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

import scala.util.Try

final case class Acl(
    profileAccess: List[ProfileAccess],
    groupAccess: Option[List[GroupAccess]],
)

object Acl {
    implicit object AclReader extends BSONDocumentReader[Acl] {

        override def readDocument(doc: BSONDocument): Try[Acl] = for {
            profileAccess <- doc.getAsTry[List[ProfileAccess]]("profileAccess")
            groupAccess = doc.getAsOpt[List[GroupAccess]]("groupAccess")
        } yield Acl(profileAccess, groupAccess)
    }

    implicit object AclWriter extends BSONDocumentWriter[Acl] {

        override def writeTry(acl: Acl): Try[BSONDocument] =
            scala.util.Success(BSONDocument(
                "profileAccess" -> acl.profileAccess,
                "groupAccess" -> acl.groupAccess
            ))
    }
}