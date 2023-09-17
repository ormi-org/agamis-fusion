package io.agamis.fusion.api.rest.model.dto.fs.file.acl.access

import io.agamis.fusion.core.db.models.documents.file.acl.access.GroupAccess
import io.agamis.fusion.api.rest.model.dto.fs.file.acl.access.rights.RightsDto
import io.agamis.fusion.api.rest.model.dto.fs.file.acl.access.rights.RightsDtoJsonProtocol._
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import java.util.UUID
import scala.language.implicitConversions

final case class GroupAccessDto (
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

    implicit val groupAccessFormat: RootJsonFormat[GroupAccessDto] = jsonFormat2(GroupAccessDto.apply)
}