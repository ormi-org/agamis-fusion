package io.agamis.fusion.external.api.rest.dto.filesystem

import java.util.UUID
import io.agamis.fusion.core.db.models.sql.FileSystem

import io.agamis.fusion.external.api.rest.dto.organization.OrganizationDto
import io.agamis.fusion.external.api.rest.dto.application.ApplicationDto

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.agamis.fusion.external.api.rest.dto.common.JsonFormatters._
import spray.json._

final case class FileSystemDto(
  id: Option[UUID],
  rootdirId: String,
  label: String,
  shared: Boolean,
  organizations: Option[List[(Boolean, OrganizationDto)]],
  licensedApplications: Option[List[ApplicationDto]]
)

object FileSystemDto {
  def from(f: FileSystem): FileSystemDto = {
    apply(
      Some(f.id),
      f.rootdirId,
      f.label,
      f.shared,
      Some(f.organizations.filter(_._1 == true).map(r => (r._2._1, OrganizationDto.from(r._2._2)))),
      Some(f.licensedApplications.map(r => ApplicationDto.from(r)))
    )
  }

  def apply(
    id: Option[UUID],
    rootdirId: String,
    label: String,
    shared: Boolean,
    organizations: Option[List[(Boolean, OrganizationDto)]],
    licensedApplications: Option[List[ApplicationDto]]
  ): FileSystemDto = {
    FileSystemDto(
      id,
      rootdirId,
      label,
      shared,
      organizations,
      licensedApplications
    )
  }
}

trait FileSystemJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  import io.agamis.fusion.external.api.rest.dto.application.ApplicationJsonProtocol._
  import io.agamis.fusion.external.api.rest.dto.organization.OrganizationJsonProtocol._

  implicit val filesystemFormat: RootJsonFormat[FileSystemDto] = jsonFormat6(
    FileSystemDto.apply
  )
}

object FileSystemJsonProtocol extends FileSystemJsonSupport
