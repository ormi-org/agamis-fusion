package io.agamis.fusion.api.rest.model.dto.permission

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.agamis.fusion.api.rest.model.dto.application.ApplicationDto
import io.agamis.fusion.api.rest.model.dto.common.JsonFormatters._
import io.agamis.fusion.api.rest.model.dto.common.LanguageMapping
import io.agamis.fusion.core.db.models.sql.Application
import io.agamis.fusion.core.db.models.sql.Permission
import spray.json._

import java.time.Instant
import java.util.UUID
import scala.collection.mutable.ListBuffer

/** Permission DTO with JSON support
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
final case class PermissionDto(
    id: Option[UUID],
    key: String,
    labels: List[LanguageMapping],
    descriptions: List[LanguageMapping],
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
          p.labels
              .foldLeft(ListBuffer.empty[LanguageMapping]) { (acc, i) =>
                  {
                      acc += new LanguageMapping(
                        i._1._1,
                        i._1._2,
                        i._2._1,
                        i._2._2
                      )
                  }
              }
              .toList,
          p.descriptions
              .foldLeft(ListBuffer.empty[LanguageMapping]) { (acc, i) =>
                  {
                      acc += new LanguageMapping(
                        i._1._1,
                        i._1._2,
                        i._2._1,
                        i._2._2
                      )
                  }
              }
              .toList,
          p.relatedApplication.collect { case a: Application =>
              ApplicationDto.from(a)
          },
          p.editable,
          Some(p.createdAt.toInstant),
          Some(p.updatedAt.toInstant)
        )
    }
}

trait PermissionJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
    import io.agamis.fusion.api.rest.model.dto.application.ApplicationJsonProtocol._
    import io.agamis.fusion.api.rest.model.dto.common.LanguageMappingJsonProtocol._

    implicit val permissionFormat: RootJsonFormat[PermissionDto] = jsonFormat8(
      PermissionDto.apply
    )
}

object PermissionJsonProtocol extends PermissionJsonSupport
