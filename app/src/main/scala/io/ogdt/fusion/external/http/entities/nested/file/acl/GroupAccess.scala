package io.ogdt.fusion.external.http.entities.nested.file.acl

import java.util.UUID

import spray.json.DefaultJsonProtocol

import io.ogdt.fusion.external.http.entities.nested.file.acl.access.Rights
import io.ogdt.fusion.external.http.entities.nested.file.acl.access.RightsJsonProtocol._

import io.ogdt.fusion.external.http.entities.common.JsonFormatters._

import io.ogdt.fusion.core.db.models.documents.nested.file.acl.{GroupAccess => GroupAccessDocument}

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

    implicit val groupAccessFormat = jsonFormat2(GroupAccess.apply)
}