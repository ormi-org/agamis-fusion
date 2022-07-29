package io.agamis.fusion.external.api.rest.dto.permission

import io.agamis.fusion.core.db.models.sql.Permission
import io.agamis.fusion.external.api.rest.dto.application.ApplicationDto
import java.util.UUID
import java.time.Instant

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.agamis.fusion.external.api.rest.dto.common.JsonFormatters._
import spray.json._
import io.agamis.fusion.core.db.models.sql.Application

/**
  * Permission DTO with JSON support
  *
  * @param id
  * @param key
  * @param labels
  * @param descriptions
  * @param relatedApplication
  * @param editable
  * @param createdAt
  * @param updateAt
  */
final case class PermissionDto (
  id: Option[UUID],
  key: String,
  labels: Map[(UUID, UUID), (String, String)],
  descriptions: Map[(UUID, UUID), (String, String)],
  relatedApplication: Option[ApplicationDto],
  editable: Boolean,
  createdAt: Option[Instant],
  updateAt: Option[Instant]
)

object PermissionDto {
  def from(p: Permission): PermissionDto = {
    PermissionDto(
      Some(p.id),
      p.key,
      p.labels,
      p.descriptions,
      p.relatedApplication.collect { case a: Application => ApplicationDto.from(a) },
      p.editable,
      Some(p.createdAt.toInstant),
      Some(p.updatedAt.toInstant)
    )
  }
}

trait PermissionJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
    import io.agamis.fusion.external.api.rest.dto.application.ApplicationJsonProtocol._

    implicit val permissionFormat: RootJsonFormat[PermissionDto] = jsonFormat8(PermissionDto.apply)
}

object PermissionJsonProtocol extends PermissionJsonSupport