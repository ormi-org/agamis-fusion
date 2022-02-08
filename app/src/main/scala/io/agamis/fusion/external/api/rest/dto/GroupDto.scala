package io.agamis.fusion.external.api.rest.dto.group

import io.agamis.fusion.core.db.models.sql.Group
import io.agamis.fusion.core.db.datastores.sql.GroupStore
import io.agamis.fusion.external.api.rest.dto.profile.ProfileDto
import java.util.UUID
import java.time.Instant

import scala.language.implicitConversions
import io.agamis.fusion.external.api.rest.dto.common.JsonFormatters._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

import scala.util.{Failure, Success}

final case class GroupDto (
    id: Option[UUID],
    name: String,
    members: Option[List[ProfileDto]],
    createdAt: Instant,
    updatedAt: Instant
)

trait GroupJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
    import io.agamis.fusion.external.api.rest.dto.profile.ProfileJsonProtocol._

    implicit val groupFormat: RootJsonFormat[GroupDto] = jsonFormat5(GroupDto)
}

object GroupJsonProtocol extends GroupJsonSupport