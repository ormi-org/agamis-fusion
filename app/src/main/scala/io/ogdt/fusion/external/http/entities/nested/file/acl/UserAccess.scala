package io.ogdt.fusion.external.http.entities.nested.file.acl

import java.util.UUID

import spray.json._
import spray.json.DefaultJsonProtocol._

import io.ogdt.fusion.external.http.entities.nested.file.acl.access.Rights
import io.ogdt.fusion.external.http.entities.nested.file.acl.access.RightsJsonProtocol._

import io.ogdt.fusion.external.http.entities.common.JsonFormatters._

import io.ogdt.fusion.core.db.models.documents.nested.file.acl.{ProfileAccess => ProfileAccessDocument}

final case class UserAccess(
    profileId: UUID,
    rights: Rights
)

object UserAccess {
    
    implicit def userAccessToDocument(ua: UserAccess): ProfileAccessDocument = {
        ProfileAccessDocument(
            ua.profileId, 
            ua.rights
        )
    }

    implicit def documentToUser(doc: ProfileAccessDocument): UserAccess = {
        UserAccess(
            doc.profileId, 
            doc.rights
        )
    }

}

object UserJsonProtocol extends DefaultJsonProtocol {

    implicit val userFormat = jsonFormat2(UserAccess.apply)

}