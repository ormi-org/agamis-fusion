package io.agamis.fusion.api.rest.model.dto.fs.file.acl.access

import io.agamis.fusion.core.db.models.documents.file.acl.access.ProfileAccess
import io.agamis.fusion.api.rest.model.dto.fs.file.acl.access.rights.RightsDto
import io.agamis.fusion.api.rest.model.dto.fs.file.acl.access.rights.RightsDtoJsonProtocol._
import spray.json._

import java.util.UUID
import scala.language.implicitConversions

final case class ProfileAccessDto (
    profileId: String,
    rights: RightsDto
)

object ProfileAccessDto {
    
    implicit def apply(dto: ProfileAccessDto): ProfileAccess = {
        ProfileAccess(
            UUID.fromString(dto.profileId),
            dto.rights
        )
    }

    implicit def apply(doc: ProfileAccess): ProfileAccessDto = {
        ProfileAccessDto(
            doc.profileId.toString,
            doc.rights
        )
    }
}

object ProfileAccessDtoJsonProtocol extends DefaultJsonProtocol {

    implicit val userFormat: RootJsonFormat[ProfileAccessDto] = jsonFormat2(ProfileAccessDto.apply)
}