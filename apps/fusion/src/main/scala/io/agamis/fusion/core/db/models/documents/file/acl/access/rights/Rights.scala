package io.agamis.fusion.core.db.models.documents.file.acl.access.rights

import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}
import scala.util.Try

final case class Rights(
    read: Boolean,
    readAndExecute: Boolean,
    write: Boolean,
    versioning: Option[Boolean],
    advancedVersioning: Option[Boolean],
    aclManagement: Boolean,
    advancedAclManagement: Boolean,
    totalControl: Boolean
)

object Rights {
    implicit object UserAccessReader extends BSONDocumentReader[Rights] {

        override def readDocument(doc: BSONDocument): Try[Rights] = for {
            read <- doc.getAsTry[Boolean]("read")
            readAndExecute <- doc.getAsTry[Boolean]("readAndExecute")
            write <- doc.getAsTry[Boolean]("write")
            versioning = doc.getAsOpt[Boolean]("versioning")
            advancedVersioning = doc.getAsOpt[Boolean]("advancedVersioning")
            aclManagement <- doc.getAsTry[Boolean]("aclManagement")
            advancedAclManagement <- doc.getAsTry[Boolean]("advancedAclManagement")
            totalControl <- doc.getAsTry[Boolean]("totalControl")
        } yield Rights(
            read,
            readAndExecute,
            write,
            versioning,
            advancedVersioning,
            aclManagement,
            advancedAclManagement,
            totalControl)
    }

    implicit object UserAccessWriter extends BSONDocumentWriter[Rights] {

        override def writeTry(rights: Rights): Try[BSONDocument] =
            scala.util.Success(BSONDocument(
                "read" -> rights.read,
                "readAndExecute" -> rights.readAndExecute,
                "write" -> rights.write,
                "versioning" -> rights.versioning,
                "advancedVersioning" -> rights.advancedVersioning,
                "aclManagement" -> rights.aclManagement,
                "advancedAclManagement" -> rights.advancedAclManagement,
                "totalControl" -> rights.totalControl,
            ))
    }
}