package io.agamis.fusion.api.rest.model.dto.fs.file.acl.access.rights

import io.agamis.fusion.core.db.models.documents.file.acl.access.rights.Rights
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.language.implicitConversions

final case class RightsDto(
    read: Boolean,
    readAndExecute: Boolean,
    write: Boolean,
    versioning: Option[Boolean],
    advancedVersioning: Option[Boolean],
    aclManagement: Boolean,
    advancedAclManagement: Boolean,
    totalControl: Boolean
)

object RightsDto {

    implicit def apply(dto: RightsDto): Rights = {
        Rights(
            dto.read,
            dto.readAndExecute,
            dto.write,
            dto.versioning,
            dto.advancedVersioning,
            dto.aclManagement,
            dto.advancedAclManagement,
            dto.totalControl
        )
    }

    implicit def apply(doc: Rights): RightsDto = {
        RightsDto(
            doc.read, 
            doc.readAndExecute, 
            doc.write, 
            doc.versioning, 
            doc.advancedVersioning, 
            doc.aclManagement, 
            doc.advancedAclManagement, 
            doc.totalControl
        )
    }
}

object RightsDtoJsonProtocol extends DefaultJsonProtocol {

    implicit val rightsFormat: RootJsonFormat[RightsDto] = jsonFormat8(RightsDto.apply)
}