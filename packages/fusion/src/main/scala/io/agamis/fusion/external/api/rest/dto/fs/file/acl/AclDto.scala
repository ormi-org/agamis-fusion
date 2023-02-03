package io.agamis.fusion.external.api.rest.dto.fs.file.acl

import io.agamis.fusion.core.db.models.documents.file.acl.access.GroupAccess
import io.agamis.fusion.core.db.models.documents.file.acl.Acl
import io.agamis.fusion.external.api.rest.dto.fs.file.acl.access.{GroupAccessDto, ProfileAccessDto}
import io.agamis.fusion.external.api.rest.dto.fs.file.acl.access.GroupAccessDtoJsonProtocol._
import io.agamis.fusion.external.api.rest.dto.fs.file.acl.access.ProfileAccessDtoJsonProtocol._
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.language.implicitConversions

final case class AclDto (
  profileAccess: List[ProfileAccessDto],
  groupAccess: Option[List[GroupAccessDto]],
)

object AclDto {

  implicit def apply(dto: AclDto): Acl = {
    Acl(
      dto.profileAccess.map(_.copy()),
      dto.groupAccess.orNull match {
        case gpAccess: List[GroupAccessDto] => Some(gpAccess.map(_.copy()))
        case null => None
      }
    )
  }

  implicit def apply(doc: Acl): AclDto = {
    AclDto(
      doc.profileAccess.map(_.copy()),
      doc.groupAccess.orNull match {
        case gpAccess: List[GroupAccess] => Some(gpAccess.map(_.copy()))
        case null => None
      }
    )
  }

}

object AclDtoJsonProtocol extends DefaultJsonProtocol {

  implicit val aclFormat: RootJsonFormat[AclDto] = jsonFormat2(AclDto.apply)
}