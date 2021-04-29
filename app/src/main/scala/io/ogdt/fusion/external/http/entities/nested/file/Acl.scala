package io.ogdt.fusion.external.http.entities.nested.file

import spray.json.DefaultJsonProtocol

import io.ogdt.fusion.external.http.entities.nested.file.acl.{GroupAccess, UserAccess}
import io.ogdt.fusion.external.http.entities.nested.file.acl.GroupJsonProtocol._
import io.ogdt.fusion.external.http.entities.nested.file.acl.UserJsonProtocol._

import io.ogdt.fusion.core.db.models.documents.nested.file.{Acl => AclDocument}

final case class Acl(
    userAccess: List[UserAccess],
    groupAccess: Option[List[GroupAccess]],
)

object Acl {
    
    implicit def acltoDocument(a: Acl): AclDocument = {
        AclDocument(
            a.userAccess.map(_.copy()),
            Some(a.groupAccess.getOrElse(null).map(_.copy()))
        )
    }

    implicit def documentToAcl(doc: AclDocument): Acl = {
        Acl(
            doc.userAccess.map(_.copy()),
            Some(doc.groupAccess.getOrElse(null).map(_.copy()))
        )
    }

}

object AclJsonProtocol extends DefaultJsonProtocol {

    implicit val aclFormat = jsonFormat2(Acl.apply)
}