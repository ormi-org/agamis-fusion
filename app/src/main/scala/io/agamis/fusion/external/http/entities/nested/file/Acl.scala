package io.agamis.fusion.external.http.entities.nested.file

import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import io.agamis.fusion.external.http.entities.nested.file.acl.{GroupAccess, UserAccess}
import io.agamis.fusion.external.http.entities.nested.file.acl.GroupJsonProtocol._
import io.agamis.fusion.external.http.entities.nested.file.acl.UserJsonProtocol._
import io.agamis.fusion.core.db.models.documents.nested.file.{Acl => AclDocument}
import io.agamis.fusion.core.db.models.documents.nested.file.acl.{GroupAccess => GroupAccessDocument}

import scala.language.implicitConversions

final case class Acl(
                      userAccess: List[UserAccess],
                      groupAccess: Option[List[GroupAccess]],
                    )

object Acl {

  implicit def acltoDocument(a: Acl): AclDocument = {
    AclDocument(
      a.userAccess.map(_.copy()),
      a.groupAccess.orNull match {
        case gpaccess: List[GroupAccess] => Some(gpaccess.map(_.copy()))
        case null => None
      }
    )
  }

  implicit def documentToAcl(doc: AclDocument): Acl = {
    Acl(
      doc.profileAccess.map(_.copy()),
      doc.groupAccess.orNull match {
        case gpaccess: List[GroupAccessDocument] => Some(gpaccess.map(_.copy()))
        case null => None
      }
    )
  }

}

object AclJsonProtocol extends DefaultJsonProtocol {

  implicit val aclFormat: RootJsonFormat[Acl] = jsonFormat2(Acl.apply)
}