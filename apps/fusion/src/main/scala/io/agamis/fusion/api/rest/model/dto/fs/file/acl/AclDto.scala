package io.agamis.fusion.api.rest.model.dto.fs.file.acl

import io.agamis.fusion.api.rest.model.dto.fs.file.acl.access.GroupAccessDto
import io.agamis.fusion.api.rest.model.dto.fs.file.acl.access.GroupAccessDtoJsonProtocol._
import io.agamis.fusion.api.rest.model.dto.fs.file.acl.access.ProfileAccessDto
import io.agamis.fusion.api.rest.model.dto.fs.file.acl.access.ProfileAccessDtoJsonProtocol._
import io.agamis.fusion.core.db.models.documents.file.acl.Acl
import io.agamis.fusion.core.db.models.documents.file.acl.access.GroupAccess
import spray.json.DefaultJsonProtocol
import spray.json.RootJsonFormat

final case class AclDto(
    profileAccess: List[ProfileAccessDto],
    groupAccess: Option[List[GroupAccessDto]]
)

object AclDto {

    implicit def apply(dto: AclDto): Acl = {
        Acl(
          dto.profileAccess.map(_.copy()),
          dto.groupAccess.orNull match {
              case gpAccess: List[GroupAccessDto] =>
                  Some(gpAccess.map(_.copy()))
              case null => None
          }
        )
    }

    implicit def apply(doc: Acl): AclDto = {
        AclDto(
          doc.profileAccess.map(_.copy()),
          doc.groupAccess.orNull match {
              case gpAccess: List[GroupAccess] => Some(gpAccess.map(_.copy()))
              case null                        => None
          }
        )
    }

}

object AclDtoJsonProtocol extends DefaultJsonProtocol {

    implicit val aclFormat: RootJsonFormat[AclDto] = jsonFormat2(AclDto.apply)
}
