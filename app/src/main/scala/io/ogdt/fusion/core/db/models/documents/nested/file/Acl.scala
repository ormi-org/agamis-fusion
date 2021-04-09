package io.ogdt.fusion.core.db.models.documents.nested.file

import io.ogdt.fusion.core.db.models.documents.nested.file.acl.{UserAccess, GroupAccess}
import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}
import scala.util.Try

final case class Acl(
    userAccess: List[UserAccess],
    groupAccess: Option[List[GroupAccess]],
)

object Acl {
    implicit object AclReader extends BSONDocumentReader[Acl] {

        override def readDocument(doc: BSONDocument): Try[Acl] = for {
            userAccess <- doc.getAsTry[List[UserAccess]]("userAccess")
            groupAccess = doc.getAsOpt[List[GroupAccess]]("groupAccess")
        } yield Acl(userAccess, groupAccess)
    }

    implicit object AclWriter extends BSONDocumentWriter[Acl] {

        override def writeTry(acl: Acl): Try[BSONDocument] =
            scala.util.Success(BSONDocument(
                "userAccess" -> acl.userAccess,
                "groupAccess" -> acl.groupAccess
            ))
    }
}