package io.ogdt.fusion.external.http.entities.nested.file

import spray.json.DefaultJsonProtocol

import io.ogdt.fusion.external.http.entities.nested.file.acl.{GroupAccess, UserAccess}
import io.ogdt.fusion.external.http.entities.nested.file.acl.GroupJsonProtocol._
import io.ogdt.fusion.external.http.entities.nested.file.acl.UserJsonProtocol._

import io.ogdt.fusion.core.db.models.documents.nested.file.{Acl => AclDocument}
import io.ogdt.fusion.core.db.models.documents.nested.file.acl.{GroupAccess => GroupAccessDocument}

final case class Acl(
    userAccess: List[UserAccess],
    groupAccess: Option[List[GroupAccess]],
)

object Acl {
    
    implicit def acltoDocument(a: Acl): AclDocument = {
        AclDocument(
            a.userAccess.map(_.copy()),
            a.groupAccess.getOrElse(null) match {
                case gpaccess: List[GroupAccess] => Some(gpaccess.map(_.copy()))
                case null => None
            }
        )
    }

    implicit def documentToAcl(doc: AclDocument): Acl = {
        Acl(
            doc.userAccess.map(_.copy()),
            doc.groupAccess.getOrElse(null) match {
                case gpaccess: List[GroupAccessDocument] => Some(gpaccess.map(_.copy()))
                case null => None
            }
        )
    }

}

object AclJsonProtocol extends DefaultJsonProtocol {

    implicit val aclFormat = jsonFormat2(Acl.apply)
}