package io.agamis.fusion.api.rest.model.dto.fs.file.acl.access

import io.agamis.fusion.api.rest.model.dto.fs.file.acl.access.rights.RightsDto
import io.agamis.fusion.api.rest.model.dto.fs.file.acl.access.rights.RightsDtoJsonProtocol._
import io.agamis.fusion.core.db.models.documents.file.acl.access.GroupAccess
import spray.json.DefaultJsonProtocol
import spray.json.RootJsonFormat

import java.util.UUID

final case class GroupAccessDto(
    groupId: String,
    rights: RightsDto
)

object GroupAccessDto {

    implicit def apply(dto: GroupAccessDto): GroupAccess = {
        GroupAccess(
          UUID.fromString(dto.groupId),
          dto.rights
        )
    }

    implicit def apply(doc: GroupAccess): GroupAccessDto = {
        GroupAccessDto(
          doc.groupId.toString,
          doc.rights
        )
    }
}

object GroupAccessDtoJsonProtocol extends DefaultJsonProtocol {

    implicit val groupAccessFormat: RootJsonFormat[GroupAccessDto] =
        jsonFormat2(GroupAccessDto.apply)
}
