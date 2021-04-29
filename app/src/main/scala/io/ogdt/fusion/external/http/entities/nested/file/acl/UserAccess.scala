package io.ogdt.fusion.external.http.entities.nested.file.acl

import java.util.UUID

import spray.json._
import spray.json.DefaultJsonProtocol._

import io.ogdt.fusion.external.http.entities.nested.file.acl.access.Rights
import io.ogdt.fusion.external.http.entities.nested.file.acl.access.RightsJsonProtocol._

import io.ogdt.fusion.external.http.entities.common.JsonFormatters._

import io.ogdt.fusion.core.db.models.documents.nested.file.acl.{UserAccess => UserAccessDocument}

final case class UserAccess(
    userId: UUID,
    rights: Rights
)

object UserAccess {
    
    implicit def userAccessToDocument(ua: UserAccess): UserAccessDocument = {
        UserAccessDocument(
            ua.userId, 
            ua.rights
        )
    }

    implicit def documentToUser(doc: UserAccessDocument): UserAccess = {
        UserAccess(
            doc.userId, 
            doc.rights
        )
    }

}

object UserJsonProtocol extends DefaultJsonProtocol {

    implicit val userFormat = jsonFormat2(UserAccess.apply)

}