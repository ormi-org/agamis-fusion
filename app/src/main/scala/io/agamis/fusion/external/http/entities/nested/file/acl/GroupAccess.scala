package io.agamis.fusion.external.http.entities.nested.file.acl

import java.util.UUID
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import io.agamis.fusion.external.http.entities.nested.file.acl.access.Rights
import io.agamis.fusion.external.http.entities.nested.file.acl.access.RightsJsonProtocol._
import io.agamis.fusion.external.http.entities.common.JsonFormatters._
import io.agamis.fusion.core.db.models.documents.nested.file.acl.{GroupAccess => GroupAccessDocument}

import scala.language.implicitConversions

final case class GroupAccess(
    groupId: UUID,
    rights: Rights
)

object GroupAccess {
    implicit def groupAccessToDocument(ga: GroupAccess): GroupAccessDocument = {
        GroupAccessDocument(
            ga.groupId,
            ga.rights
        )
    }

    implicit def documentToGroupAccess(doc: GroupAccessDocument): GroupAccess = {
        GroupAccess(
            doc.groupId,
            doc.rights
        )
    }
}

object GroupJsonProtocol extends DefaultJsonProtocol {

    implicit val groupAccessFormat: RootJsonFormat[GroupAccess] = jsonFormat2(GroupAccess.apply)
}