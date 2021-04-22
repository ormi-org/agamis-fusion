package io.ogdt.fusion.external.http.entities

import reactivemongo.api.bson.BSONObjectID
import java.util.UUID

final case class File(
    id: BSONObjectID,
    name: String,
    `type`: String,
    path: Option[String],
    parent: Option[BSONObjectID],
    chunkList: Option[List[UUID]],
    metadata: Metadata,
    versioned: Option[Boolean],
    acl: Acl,
    owner: UUID
)

object FileJsonReader {

}

object File