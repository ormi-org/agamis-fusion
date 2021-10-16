package io.ogdt.fusion.external.http.entities.nested.file.acl.access

import spray.json.DefaultJsonProtocol

import io.ogdt.fusion.core.db.models.documents.nested.file.acl.access.{Rights => RightsDocument}

final case class Rights(
    read: Boolean,
    readAndExecute: Boolean,
    write: Boolean,
    versioning: Option[Boolean],
    advancedVersioning: Option[Boolean],
    aclManagement: Boolean,
    advancedAclManagement: Boolean,
    totalControl: Boolean
)

object Rights {

    implicit def rightsToDocument(r: Rights): RightsDocument = {
        RightsDocument(
            r.read, 
            r.readAndExecute, 
            r.write, 
            r.versioning, 
            r.advancedVersioning, 
            r.aclManagement, 
            r.advancedAclManagement, 
            r.totalControl
        )
    }

    implicit def documentToRights(doc: RightsDocument): Rights = {
        Rights(
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

object RightsJsonProtocol extends DefaultJsonProtocol {

    implicit val rightsFormat = jsonFormat8(Rights.apply)
}